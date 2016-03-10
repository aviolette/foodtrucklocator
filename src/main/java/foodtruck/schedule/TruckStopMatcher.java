package foodtruck.schedule;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.TruckDAO;
import foodtruck.email.EmailNotifier;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.DayOfWeek;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.util.Clock;

/**
 * Matches a tweet to a location, truck and time.
 * @author aviolette@gmail.com
 * @since 9/22/11
 */
public class TruckStopMatcher {
  public static final String TIME_PATTERN = "(\\d+(:\\d+)*\\s*(p|pm|a|am|a\\.m\\.|p\\.m\\.)?)|noon";
  static final String TIME_PATTERN_STRICT =
      "(\\d+:\\d+\\s*(p|pm|a|am|a\\.m\\.|p\\.m\\.)?)|noon|(\\d+\\s*(p|pm|a|am|a\\.m\\.|p\\.m\\.)|((11|12|1|2|3|4|5|6)\\b))";
  private static final Logger log = Logger.getLogger(TruckStopMatcher.class.getName());
  private static final long ONE_HOUR_IN_MILLIS = 3600000;
  private static final long DEFAULT_STOP_LENGTH_IN_HOURS = 2 * ONE_HOUR_IN_MILLIS;
  private static final String TIME_RANGE_PATTERN =
      "(" + TIME_PATTERN + ")\\s*-\\s*(" + TIME_PATTERN + ")[\\s|\\$|\\n|,|\\.&&[^\\-]]";
  private static final String TOMORROW = "2morrow|tmw|tmrw|tomorrow|maana|mañana";
  private final AddressExtractor addressExtractor;
  private final GeoLocator geoLocator;
  private final DateTimeFormatter formatter;
  private final Clock clock;
  private final EmailNotifier notifier;
  private final Location center;
  private final LocalTime defaultLunchTime;
  private final ImmutableList<Spot> BEAVER_SPOTS = ImmutableList.of(
      new Spot("600w", "600 West Chicago Avenue, Chicago, IL"),
      new Spot("wabash/vanburen", "Wabash and Van Buren, Chicago, IL"),
      new Spot("wacker/adams", "Wacker and Adams, Chicago, IL"),
      new Spot("clark/adams", "Clark and Adams, Chicago, IL"),
      new Spot("lasalle/adams", "Lasalle and Adams, Chicago, IL"),
      new Spot("clark/monroe", "Clark and Monroe, Chicago, IL"),
      new Spot("wabash/jackson", "Wabash and Jackson, Chicago, IL"),
      new Spot("58th/ellis", "University of Chicago"));
  private final Pattern endTimePattern = Pattern.compile("\\b(close at|leaving at|until|til|till) (" + TIME_PATTERN + ")"),
      timeRangePattern = Pattern.compile(TIME_RANGE_PATTERN, Pattern.CASE_INSENSITIVE),
      atTimePattern = Pattern.compile("\\b(be at|ETA|open at|opening at|opens at|arrive at|there at) (" + TIME_PATTERN_STRICT + ")"),
      schedulePattern = Pattern.compile(".*M:.+(\\b|\\n)T:.+(\\b|\\n)W:.+"),
      simpleDateParser = Pattern.compile("(\\d{1,2})/(\\d{1,2})");
  private final TruckDAO truckDAO;

  @Inject
  public TruckStopMatcher(AddressExtractor extractor, GeoLocator geoLocator, DateTimeZone defaultZone, Clock clock,
      EmailNotifier notifier, @Named("center") Location center, @DefaultStartTime LocalTime startTime,
      TruckDAO truckDAO) {
    this.addressExtractor = extractor;
    this.geoLocator = geoLocator;
    this.center = center;
    this.defaultLunchTime = startTime;
    formatter = DateTimeFormat.forPattern("hhmma").withZone(defaultZone);
    this.clock = clock;
    this.notifier = notifier;
    this.truckDAO = truckDAO;
  }

  /**
   * Matches a truck to a location via a story.
   * @param truck a truck
   * @param story a story
   * @return a TruckStopMatch if the match can be made, otherwise {@code null}
   */
  public @Nullable TruckStopMatch match(Truck truck, Story story) {
    TruckStop.Builder truckStopBuilder = TruckStop.builder()
        .origin(StopOrigin.TWITTER)
        .truck(truck);
    try {
      // Ignore story if it's a retweet of another story
      checkIfShared(story);

      // Some trucks will only count if they contain a certain regular expression; conversely, we want to filter
      // out tweets where regular expressions don't match
      final String lowerCaseTweet = story.getText().toLowerCase();
      verifyMatchOnlyExpression(truck, story, lowerCaseTweet);

      // Some tweets indicate a daily schedule or a set of stops...skip those
      verifySchedule(story);

      // Some tweets are talking about happenings on other days...skip those too
      verifyOtherDay(story);

      // Get the location out of the story
      Location location = extractLocation(story, truck);
      verifyLocation(location);
      truckStopBuilder.location(location);

      // Get the time out of the story
      boolean softEnding = extractDateTime(truck, story, truckStopBuilder);

      TruckStopMatch.Builder builder = TruckStopMatch.builder()
            .stop(truckStopBuilder.notes(
                ImmutableList.of(String.format("Tweet received for location: '%s'", story.getText()))).build())
            .text(story.getText())
            .softEnding(softEnding)
            .tweetId(story.getId());

      // Special handling for beavers since they tweet out multiple stops in one tweet at the beginning of the day
      // in a fairly predictable way
      handleBeavers(builder, story, truck);

      handleLaJefa(builder, story, truck);

      return builder.build();
    } catch (UnmatchedException e) {
      log.info(e.getMessage());
      return null;
    }
  }

  private void handleLaJefa(TruckStopMatch.Builder builder, Story story, Truck truck) {
    if(!"patronachicago".equals(truck.getId())) {
      return;
    }
    String lowerTweet = story.getText().toLowerCase();
    String stripped = lowerTweet.replace(" ", "");

    int index = stripped.indexOf("lajefa");
    if (index == -1) {
      return;
    }
    stripped = stripped.substring(index);

    Truck laJefa = truckDAO.findById("lajefa");
    if (laJefa == null) {
      log.warning("La Jefa food truck not found.");
      return;
    }

    TruckStop primary = builder.getPrimaryStop();

    for (Spot spot : BEAVER_SPOTS) {
      if (spot.contains(stripped)) {
        builder.appendStop(TruckStop.builder()
            .startTime(primary.getStartTime())
            .endTime(primary.getEndTime())
            .origin(StopOrigin.TWITTER)
            .truck(laJefa)
            .locked(true)
            .location(geoLocator.locate(spot.getCanonicalForm(), GeolocationGranularity.NARROW))
            .build());
        return;
      }
    }
  }

  private void handleBeavers(TruckStopMatch.Builder builder, Story story, Truck truck) {
    if (!"beaversdonuts".equals(truck.getId())) {
      return;
    }

    String lowerTweet = story.getText().toLowerCase();
    String stripped = lowerTweet.replace(" ", "");
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    if (lowerTweet.contains("wacker") && lowerTweet.contains("madison")) {
      stops.add(TruckStop.builder()
          .startTime(story.getTime().withTime(7, 0, 0, 0))
          .endTime(story.getTime().withTime(10, 0, 0, 0))
          .origin(StopOrigin.TWITTER)
          .truck(truck)
          .locked(true)
          .location(geoLocator.locate("Madison and Wacker, Chicago, IL", GeolocationGranularity.NARROW))
          .build());
    }

    for (Spot spot : BEAVER_SPOTS) {
      if (spot.contains(stripped)) {
        stops.add(TruckStop.builder()
            .startTime(story.getTime().withTime(7, 0, 0, 0))
            .endTime(story.getTime().withTime(14, 0, 0, 0))
            .origin(StopOrigin.TWITTER)
            .truck(truck)
            .locked(true)
            .location(geoLocator.locate(spot.getCanonicalForm(), GeolocationGranularity.NARROW))
            .build());
      }
    }

    if (lowerTweet.contains("sangamon") && lowerTweet.contains("southport") && story.getTime().getHourOfDay() < 13) {
      builder.softEnding(false);
      stops.add(TruckStop.builder()
          .endTime(story.getTime().withTime(14, 0, 0, 0))
          .startTime(story.getTime().withTime(8, 0, 0, 0))
          .truck(truck)
          .origin(StopOrigin.TWITTER)
          .locked(true)
          .location(geoLocator.locate("Southport and Addison, Chicago, IL", GeolocationGranularity.NARROW))
          .build());
      stops.add(TruckStop.builder()
          .endTime(story.getTime().withTime(13, 0, 0, 0))
          .startTime(story.getTime().withTime(8, 0, 0, 0))
          .truck(truck)
          .locked(true)
          .location(geoLocator.locate("Sangamon and Monroe, Chicago, IL", GeolocationGranularity.NARROW))
          .build());
    }
    ImmutableList<TruckStop> truckStops = stops.build();
    if (truckStops.size() > 1) {
      builder.softEnding(false);
      builder.stops(truckStops);
    }
  }

  private void verifyOtherDay(Story tweet) throws UnmatchedException {
    if (matchesOtherDay(tweet.getText())) {
      throw new UnmatchedException(String.format("Didn't match '%s' because it contained a day of the week", tweet));
    }
  }

  private void verifySchedule(Story tweet) throws UnmatchedException {
    String lowerCaseTweet = tweet.getText().toLowerCase();
    final boolean morning = isMorning(tweet.getTime());
    if (lowerCaseTweet.contains("stops") ||
        (morning && (lowerCaseTweet.contains("schedule"))) || containsAbbreviatedSchedule(tweet.getText())) {
      throw new UnmatchedException(String.format("Ignoring '%s' because the word 'schedule' is there", tweet.getText()));
    }
  }

  private boolean extractDateTime(Truck truck, Story tweet, TruckStop.Builder tsBuilder) throws UnmatchedException {
    // Handling time ranges (e.g. 11am-2pm)
    handleTimeRange(tweet, tsBuilder);

    // Handling start-time only
    handleStartTime(truck, tweet, tsBuilder);

    // If there's no start time, just start it now or (if certain rules apply) at lunch hour
    handleImmediateStart(truck, tweet, tsBuilder);

    // See if we can parse the end time out if it wasn't specified as a range earlier
    handleSeparateEndTime(tweet, tsBuilder);

    // If there is no end time, just guess
    return handleSoftEnding(truck, tsBuilder);
  }

  private boolean handleSoftEnding(Truck truck, TruckStop.Builder tsBuilder) {
    if (tsBuilder.endTime() != null) {
      return false;
    }
    // If it's a lunch truck, extend its time to a min of 1pm.
    if (((tsBuilder.startTime().getHourOfDay() == 10 && tsBuilder.startTime().getMinuteOfHour() >= 30) ||
        (tsBuilder.startTime().getHourOfDay() == 11 && tsBuilder.startTime().getMonthOfYear() == 0))
        && truck.getCategoryList().contains("Lunch")) {
      tsBuilder.endTime(tsBuilder.startTime().withTime(14, 0, 0, 0));
    } else {
      tsBuilder.endTime(tsBuilder.startTime().plus(stopTime(truck, tsBuilder.startTime())));
    }
    return true;
  }

  private void handleSeparateEndTime(Story tweet, TruckStop.Builder tsBuilder) {
    if (tsBuilder.endTime() == null) {
      final DateTime endTime = parseEndTime(tweet.getText(), tsBuilder.startTime());
      tsBuilder.endTime(endTime);
      if (tsBuilder.hasTimes() &&
          (tsBuilder.startTime().isAfter(endTime) && endTime.isAfter(tweet.getTime()))) {
        tsBuilder.startTime(tweet.getTime());
      }
    }

  }

  private void handleImmediateStart(Truck truck, Story tweet, TruckStop.Builder tsBuilder) {
    if (tsBuilder.startTime() != null) {
      return;
    }
    // Cupcake trucks and such should not be matched at all by this rule since they make many frequent stops
    if (canStartNow(truck, isMorning(tweet.getTime()), tweet.getText())) {
      tsBuilder.startTime(tweet.getTime());
    } else {
      tsBuilder.startTime(tweet.getTime().withTime(defaultLunchTime));
      tsBuilder.endTime(null);
    }
  }

  private void handleStartTime(Truck truck, Story tweet, TruckStop.Builder tsBuilder) {
    Matcher m;
    // This is detecting something in the format: We will be at Merchandise mart at 11:00.
    if (tsBuilder.startTime() == null) {
      m = atTimePattern.matcher(tweet.getText());
      if (m.find()) {
        final LocalDate date = tweet.getTime().toLocalDate();
        tsBuilder.startTime(parseTime(m.group(2), date, null));
        if (tsBuilder.startTime() != null) {
          if (tsBuilder.startTime().getHourOfDay() == 0) {
            tsBuilder.startTime(tsBuilder.startTime().withHourOfDay(12));
          }
          tsBuilder.endTime(tsBuilder.startTime().plus(stopTime(truck, tsBuilder.startTime())));
        }
        // This is a special case, since matching ranges like that will produce a lot of
        // false positives, but 11-1 is commonly used for lunch hour
      } else if (tweet.getText().contains("11-1")) {
        tsBuilder.startTime(clock.currentDay().toDateTime(new LocalTime(11, 0), clock.zone()));
        tsBuilder.endTime(clock.currentDay().toDateTime(new LocalTime(13, 0), clock.zone()));
      } else if (tweet.getText().contains("11-2")) {
        tsBuilder.startTime(clock.currentDay().toDateTime(new LocalTime(11, 0), clock.zone()));
        tsBuilder.endTime(clock.currentDay().toDateTime(new LocalTime(14, 0), clock.zone()));
      } else if (tweet.getText().contains("11a") && truck.getCategories().contains("Lunch")) {
        tsBuilder.startTime(clock.currentDay().toDateTime(new LocalTime(11, 0), clock.zone()));
        tsBuilder.endTime(clock.currentDay().toDateTime(new LocalTime(13, 0), clock.zone()));
      } else if (tweet.getTime().getHourOfDay() > 12 && tweet.getTime().getHourOfDay() < 17 &&
          (tweet.getText().contains("tonight") || tweet.getText().contains("tonite"))) {
        tsBuilder.startTime(clock.currentDay().toDateTime(new LocalTime(17, 30), clock.zone()));
      }
    }
  }

  private void handleTimeRange(Story tweet, TruckStop.Builder tsBuilder) {
    Matcher m = timeRangePattern.matcher(tweet.getText() + " ");
    if (m.find()) {
      final LocalDate date = tweet.getTime().toLocalDate();
      tsBuilder.startTime(parseTime(m.group(1), date, null));
      tsBuilder.endTime(parseTime(m.group(5), date, tsBuilder.startTime()));
      if (tsBuilder.hasTimes() && tsBuilder.startTime().getHourOfDay() > 12 &&
          tsBuilder.startTime().isAfter(tsBuilder.endTime())) {
        tsBuilder.startTime(tsBuilder.startTime().minusHours(12));
      } else if (tsBuilder.hasTimes() && tsBuilder.endTime().isBefore(tweet.getTime())) {
        Duration duration = new Duration(tsBuilder.startTime().toInstant(),
            tsBuilder.endTime().toInstant());
        tsBuilder.startTime(tsBuilder.startTime().plusHours(12));
        tsBuilder.endTime(tsBuilder.startTime().plus(duration));
      }
    }
  }

  private void verifyLocation(Location location) throws UnmatchedException {
    if (location == null) {
      throw new UnmatchedException("Location is not specified");
    }
    if (location.distanceFrom(center) > 50.0d) {
      throw new UnmatchedException("Center greater than 50 miles");
    }
  }

  private void checkIfShared(Story tweet) throws UnmatchedException {
    if (tweet.isManualRetweet()) {
      throw new UnmatchedException("Retweeted: " + tweet.getText());
    }
  }

  private boolean isMorning(DateTime time) {
    LocalTime lt = time.toLocalTime();
    // anything before 4 counts as the previous night
    return lt.isAfter(new LocalTime(4, 0)) && lt.isBefore(new LocalTime(10, 30));
  }

  /**
   * Tests for tweets like this: <code></code>THE TRUCK: M: 600 W Chicago T: NBC Tower W: Clark & Monroe
   * TH: Madison & Wacker + Montrose & Ravenswood (5pm) F: Lake & Wabash</code>
   */
  private boolean containsAbbreviatedSchedule(String tweetText) {
    return schedulePattern.matcher(tweetText).find();
  }

  private long stopTime(Truck truck, DateTime startTime) {
    if (startTime.getHourOfDay() < 11 && truck.getCategories().contains("MorningSquatter")) {
      DateTime maxDay = startTime.withTime(14, 0, 0, 0);
      return Math.max(maxDay.getMillis() - startTime.getMillis(), DEFAULT_STOP_LENGTH_IN_HOURS);
    } else if(startTime.getHourOfDay() == 6) {
      // if starts at 6am hour, extend to 9am instead of something like 8:43am or something.
      DateTime maxDay = startTime.withTime(9, 0, 0, 0);
      return Math.max(maxDay.getMillis() - startTime.getMillis(), DEFAULT_STOP_LENGTH_IN_HOURS);
    }
    return truck.getCategories().contains("1HRStops") ? ONE_HOUR_IN_MILLIS : DEFAULT_STOP_LENGTH_IN_HOURS;
  }

  private boolean canStartNow(Truck truck, boolean morning, String tweetText) {
    if (!morning) {
      return true;
    }
    Set<String> categories = truck.getCategories();
    boolean breakfast = categories.contains("Breakfast");
    if (breakfast && categories.contains("Lunch")) {
      String lower = tweetText.toLowerCase();
      return lower.matches(".*b(\\w*|')fast.*") || lower.contains("open for b") || lower.contains("brunch")
          || lower.contains("mornin") || lower.contains("biscuit") || lower.matches(".*rise\\s*(&|and)\\s*shine.*");
    } else if (breakfast) {
      return true;
    }
    return false;
  }

  private void verifyMatchOnlyExpression(Truck truck, Story tweet, String lower) throws UnmatchedException {
    Pattern p = truck.getMatchOnlyIf();
    if (p != null) {
      Matcher m = p.matcher(lower);
      if (!m.find()) {
        throw new UnmatchedException(String.format("Match-only-if expression '%s' not found for tweet '%s'",
            truck.getMatchOnlyIfString(), tweet.getText()));
      }
    }
    p = truck.getDonotMatchIf();
    if (p != null) {
      Matcher m = p.matcher(lower);
      if ( m.find()) {
        throw new UnmatchedException(String.format("Do-not-match-if expression '%s' found in tweet '%s'", truck.getDonotMatchIfString(), tweet.getText()));
      }
    }
  }

  private boolean matchesOtherDay(String tweetText) {
    Matcher matcher = simpleDateParser.matcher(tweetText);
    // matches date pattern
    if (matcher.find()) {
      LocalDate date = clock.currentDay();
      String first = matcher.group(1);
      String second = matcher.group(2);
      if (Integer.parseInt(first) != date.getMonthOfYear() || Integer.parseInt(second) != date.getDayOfMonth()) {
        return true;
      }
    }

    // check to see if it has a day-of-the-week in the tweet (or 'tomorrow') that's not today
    StringBuilder pattern = new StringBuilder();
    pattern.append("\\b(");
    DayOfWeek now = clock.dayOfWeek();
    for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
      if (dayOfWeek != now) {
        pattern.append(dayOfWeek.getMatchPattern()).append("|");
      }
    }
    pattern.append(TOMORROW).append(")\\b");
    return Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE).matcher(tweetText).find();
  }

  private @Nullable Location extractLocation(Story tweet, Truck truck) {
    List<String> addresses = addressExtractor.parse(tweet.getSanitizedText(), truck);
    Location tweetLocation = tweet.getLocation();
    if (tweetLocation != null) {
      log.info("Location data enabled for tweet from " + truck.getId());
      // Currently not using this function...remove next line to re-enabled
    }
    log.log(Level.INFO, "Extracted these addresses: {0} from tweet: {1}", new Object[] {addresses, tweet.getText()});
    for (String address : addresses) {
      Location loc = geoLocator.locate(address, GeolocationGranularity.NARROW);
      if (loc != null && loc.isResolved()) {
        if (loc.isJustResolved()) {
          this.notifier.systemNotifyLocationAdded(loc, tweet, truck);
        }
        return loc;
      }
    }
    return null;
  }

  private @Nullable DateTime parseTime(String timeText, LocalDate date, @Nullable DateTime after) {
    int plusDay = 0;
    if (timeText.toLowerCase().equals("noon")) {
      timeText = "12:00p.m.";
    }
    int lineBreak = timeText.indexOf('\n');
    if (lineBreak != -1) {
      timeText = timeText.substring(0, lineBreak);
    }
    timeText = timeText.replace(".", "");
    if (timeText.endsWith("p") || timeText.endsWith("a")) {
      timeText = timeText + "m";
    }
    timeText = timeText.replace(":", "");
    timeText = timeText.replace(" ", "");
    timeText = timeText.toLowerCase();
    timeText = timeText.trim();
    String tmpTime = timeText;
    String suffix = null;
    if (timeText.endsWith("pm") || timeText.endsWith("am")) {
      tmpTime = timeText.substring(0, timeText.length() - 2);
      suffix = timeText.substring(timeText.length() - 2);
    }
    switch (tmpTime.length()) {
      case 1:
        tmpTime = "0" + tmpTime + "00";
        break;
      case 2:
        tmpTime = tmpTime + "00";
        break;
      case 3:
        tmpTime = "0" + tmpTime;
        break;
    }

    if (suffix != null) {
    } else if (after != null) {
      // TODO: handle military time
      tmpTime = tmpTime.trim();
      if (tmpTime.length() == 3) {
        tmpTime = "0" + tmpTime;
      }
      int hour = Integer.parseInt(tmpTime.substring(0, 2).trim());
      int min = Integer.parseInt(tmpTime.substring(2, 4).trim());
      if (after.isAfter(date.toDateTime(new LocalTime(hour, min), clock.zone()))) {
        suffix = "pm";
      } else if (hour == 12) {
        if (after.getHourOfDay() < 13) {
          suffix = "pm";
        } else {
          suffix = "am";
          plusDay++;
        }
      } else {
        suffix = "am";
      }
    } else {
      int hour = Integer.parseInt(tmpTime.substring(0, 2));
      suffix = (hour > 8 && hour < 12) ? "am" : "pm";
    }
    timeText = tmpTime + suffix;
    try {
      return formatter.parseDateTime(timeText).withDate(date.getYear(), date.getMonthOfYear(),
          date.getDayOfMonth()).plusDays(plusDay);
    } catch (IllegalArgumentException iae) {

      log.log(Level.WARNING, iae.getMessage(), iae);
      return null;
    }
  }

  private @Nullable DateTime parseEndTime(String tweetText, DateTime startTime) {
    Matcher matcher = endTimePattern.matcher(tweetText.toLowerCase());
    if (matcher.find()) {
      DateTime dt = parseTime(matcher.group(2), startTime.toLocalDate(), startTime);
      if (dt == null) {
        return null;
      }
      if (startTime.isAfter(dt)) {
        return dt.plusHours(12);
      }
      return dt;
    }
    return null;
  }

  static class UnmatchedException extends Exception {
    public UnmatchedException(String msg) {
      super(msg);
    }
  }

  static class Spot {
    public final String searchForm;
    public final String canonicalForm;

    public Spot(String searchForm, String canonicalForm) {
      this.searchForm = searchForm;
      this.canonicalForm = canonicalForm;
    }

    public boolean contains(String stripped) {
      if (process(stripped, searchForm)) {
        return true;
      }
      String split[] = searchForm.split("/");
      if (split.length < 2) {
        return false;
      }
      return process(stripped, split[1] + "/" + split[0]);
    }

    private boolean process(String stripped, String spot) {
      return stripped.contains(spot) ||
          stripped.contains(spot.replace("/", "and")) ||
          stripped.contains(spot.replace("/", "&"));

    }
    public String getCanonicalForm() {
      return canonicalForm;
    }
  }
}
