package foodtruck.schedule;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.email.EmailNotifier;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.model.TweetSummary;
import foodtruck.util.Clock;

/**
 * Matches a tweet to a location, truck and time.
 * @author aviolette@gmail.com
 * @since 9/22/11
 */
public class TruckStopMatcher {
  private static final Logger log = Logger.getLogger(TruckStopMatcher.class.getName());
  public static final int DEFAULT_STOP_LENGTH_IN_HOURS = 2;
  public static final String TIME_PATTERN = "(\\d+(:\\d+)*\\s*(p|pm|a|am|a\\.m\\.|p\\.m\\.)?)|noon";
  static final String TIME_PATTERN_STRICT =
      "(\\d+:\\d+\\s*(p|pm|a|am|a\\.m\\.|p\\.m\\.)?)|noon|(\\d+\\s*(p|pm|a|am|a\\.m\\.|p\\.m\\.)|((11|12|1|2|3|4|5|6)\\b))";
  private static final String TIME_RANGE_PATTERN =
      "(" + TIME_PATTERN + ")\\s*-\\s*(" + TIME_PATTERN + ")[\\s|,|\\.&&[^\\-]]";
  private static final String TOMORROW = "2morrow|tmw|tmrw|tomorrow|maana|mañana";
  private final AddressExtractor addressExtractor;
  private final GeoLocator geoLocator;
  private final Pattern endTimePattern;
  private final DateTimeFormatter formatter;
  private final Clock clock;
  private final Pattern timeRangePattern;
  private final Pattern monPattern;
  private final Pattern tuesPattern;
  private final Pattern wedPattern;
  private final Pattern thursPattern;
  private final Pattern friPattern;
  private final Pattern satPattern;
  private final Pattern sunPattern;
  private final Pattern atTimePattern;
  private final Pattern schedulePattern = Pattern.compile(".*M:.+(\\b|\\n)T:.+(\\b|\\n)W:.+");
  private final EmailNotifier notifier;
  private final Pattern simpleDateParser;
  private final Location center;

  @Inject
  public TruckStopMatcher(AddressExtractor extractor, GeoLocator geoLocator, DateTimeZone defaultZone, Clock clock,
      EmailNotifier notifier, @Named("center") Location center) {
    this.addressExtractor = extractor;
    this.geoLocator = geoLocator;
    this.center = center;
    this.atTimePattern = Pattern.compile("\\b(be at|ETA|open at|opening at|opens at|arrive at|there at) (" + TIME_PATTERN_STRICT + ")");
    this.endTimePattern = Pattern.compile("\\b(close at|leaving at|until|til|till) (" + TIME_PATTERN + ")");
    this.timeRangePattern = Pattern.compile(TIME_RANGE_PATTERN, Pattern.CASE_INSENSITIVE);
    this.simpleDateParser = Pattern.compile("(\\d{1,2})/(\\d{1,2})");
    this.monPattern = Pattern.compile(
        "\\b(TUE|Tu|WED|Weds|THU|Th|FRI|SAT|Sa|SUN|Su|tuesday|wednesday|thursday|friday|saturday|sunday|tues|thurs|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    this.tuesPattern = Pattern.compile(
        "\\b(MON|WED|Weds|THU|Th|FRI|SAT|Sa|SUN|Su|monday|wednesday|thursday|friday|saturday|sunday|thurs|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    this.wedPattern = Pattern.compile(
        "\\b(MON|TUE|Tu|THU|Th|FRI|SAT|Sa|SUN|Su|monday|tuesday|thursday|friday|saturday|sunday|tues|thurs|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    this.thursPattern = Pattern.compile(
        "\\b(MON|TUE|Tu|WED|Weds|FRI|SAT|Sa|SUN|Su|monday|tuesday|wednesday|friday|saturday|sunday|tues|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    this.friPattern = Pattern.compile(
        "\\b(MON|TUE|Tu|WED|Weds|THU|Th|SAT|Sa|SUN|Su|monday|tuesday|wednesday|thursday|saturday|sunday|tues|thurs|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    this.satPattern = Pattern.compile(
        "\\b(MON|TUE|Tu|WED|Weds|THU|Th|FRI|SUN|Su|monday|tuesday|wednesday|thursday|friday|sunday|tues|thurs|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    this.sunPattern = Pattern.compile(
        "\\b(MON|TUE|Tu|WED|Weds|THU|Th|FRI|SAT|Sa|monday|tuesday|wednesday|thursday|friday|saturday|tues|thurs|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    formatter = DateTimeFormat.forPattern("hhmma").withZone(defaultZone);
    this.clock = clock;
    this.notifier = notifier;
  }

  public Location getMapCenter() {
    return center;
  }

  /**
   * Matches a truck to a location via a tweet.
   * @param truck a truck
   * @param tweet a tweet
   * @param terminationTime an optional time to terminate any match
   * @return a TruckStopMatch if the match can be made, otherwise {@code null}
   */
  public @Nullable TruckStopMatch match(Truck truck, TweetSummary tweet,
      @Nullable DateTime terminationTime) {
    final String tweetText = tweet.getText();
    if (tweet.isManualRetweet()) {
      log.log(Level.INFO, "Didn't match '{0}' because it is a retweet",
          tweetText);
      return null;
    }
    final String lowerCaseTweet = tweetText.toLowerCase();
    Confidence confidence = Confidence.LOW;
    List<String> notes = Lists.newLinkedList();
    // Some trucks, like Starfruit Cafe have configurable expressions (e.g. #KefirTruck) that
    // should be present in order to do a match
    notes.add(String.format("Tweet received for location: '%s'", tweetText));
    if (verifyMatchOnlyExpression(truck, tweetText)) {
      log.log(Level.INFO, "Didn't match '{0}' because it didn't contain match-only expression",
          tweetText);
      return null;
    } else if (truck.getMatchOnlyIf() != null || lowerCaseTweet.contains("#ftf")
        || lowerCaseTweet.contains("#foodtruckfinder")) {
      confidence = confidence.up();
      notes.add("Presense of specific hash tag in tweet increased confidence.");
    }

    Location location = extractLocation(tweet, truck);
    if (location == null || location.distanceFrom(getMapCenter()) > 50.0d) {
      return null;
    }
    DateTime startTime = null;
    final boolean morning = isMorning(tweet.getTime());
    // TODO: this signals a schedule being tweeted, for now we can't handle that
    if (lowerCaseTweet.contains("stops") ||
        (morning && (lowerCaseTweet.contains("schedule"))) || containsAbbreviatedSchedule(tweetText)) {
      return null;
    }
    // If we're on Tuesday and their tweeting about Wednesday, skip.
    if (matchesOtherDay(tweetText)) {
      log.log(Level.INFO, "Didn't match '{0}' because it contained a day of the week", tweetText);
      return null;
    }

    // TODO: this is kind of a hack - this is a tweet announcing a future lunch location
    DateTime endTime = null;
    Matcher m = timeRangePattern.matcher(tweetText);
    if (m.find()) {
      final LocalDate date = tweet.getTime().toLocalDate();
      startTime = parseTime(m.group(1), date, null);
      endTime = parseTime(m.group(5), date, startTime);
      if (endTime != null && terminationTime != null && endTime.isAfter(terminationTime)) {
        endTime = terminationTime;
      } else if (startTime != null && endTime != null && terminationTime == null &&
          startTime.getHourOfDay() > 12 && endTime.getHourOfDay() < 12) {
          startTime = startTime.minusHours(12);
      } else if (endTime != null && startTime != null && endTime.isBefore(tweet.getTime())) {
        Duration duration = new Duration(startTime.toInstant(), endTime.toInstant());
        startTime = startTime.plusHours(12);
        endTime = startTime.plus(duration);
      }
      notes.add("Presence of time range in tweet increased confidence.");
      confidence = confidence.up();
    }
    // This is detecting something in the format: We will be at Merchandise mart at 11:00.
    if (startTime == null) {
      m = atTimePattern.matcher(tweetText);
      if (m.find()) {
        final LocalDate date = tweet.getTime().toLocalDate();
        startTime = parseTime(m.group(2), date, null);
        if (startTime != null) {
          if (startTime.getHourOfDay() == 0) {
            startTime = startTime.withHourOfDay(12);
          }
          notes.add("Presence of start time in tweet increased confidence.");
          confidence = confidence.up();
          endTime = startTime.plusHours(stopTime(truck, startTime));
        }
        // This is a special case, since matching ranges like that will produce a lot of
        // false positives, but 11-1 is commonly used for lunch hour
      } else if (tweetText.contains("11-1")) {
        startTime = clock.currentDay().toDateTime(new LocalTime(11, 0), clock.zone());
        endTime = clock.currentDay().toDateTime(new LocalTime(13, 0), clock.zone());
        notes.add("Presence of start time in tweet increased confidence.");
        confidence = confidence.up();
      } else if (tweetText.contains("11-2")) {
        startTime = clock.currentDay().toDateTime(new LocalTime(11, 0), clock.zone());
        endTime = clock.currentDay().toDateTime(new LocalTime(14, 0), clock.zone());
        notes.add("Presence of start time in tweet increased confidence.");
        confidence = confidence.up();
      } else if (tweetText.contains("11a") && truck.getCategories().contains("Lunch")) {
        startTime = clock.currentDay().toDateTime(new LocalTime(11, 0), clock.zone());
        endTime = clock.currentDay().toDateTime(new LocalTime(13, 0), clock.zone());
        notes.add("Presence of start time in tweet increased confidence.");
        confidence = confidence.up();
      } else if (tweet.getTime().getHourOfDay() > 12 && tweet.getTime().getHourOfDay() < 17 &&
          (tweetText.contains("tonight") || tweetText.contains("tonite"))) {
        startTime = clock.currentDay().toDateTime(new LocalTime(17, 30), clock.zone());
      }
    }
    if (startTime == null) {
      // Cupcake trucks and such should not be matched at all by this rule since they make many frequent stops
      if (canStartNow(truck, morning, tweetText)) {
        startTime = tweet.getTime();
      } else {
        startTime = tweet.getTime().withTime(11, 30, 0, 0);
        endTime = null;
      }
      if (!morning && truck.getCategories().contains("Dessert")) {
        confidence = confidence.up();
      }
    }
    TruckStopMatch.Builder matchBuilder = TruckStopMatch.builder();
    if (endTime == null) {
      final DateTime parsedEndTime = parseEndTime(tweetText, startTime);
      if (parsedEndTime != null) {
        notes.add("Presence of end time in tweet increased confidence.");
        confidence = confidence.up();
      }
      endTime = terminationTime == null ? parsedEndTime : terminationTime;
      if (endTime != null && startTime != null && (startTime.isAfter(endTime) && endTime.isAfter(tweet.getTime()))) {
        startTime = tweet.getTime();
      }
    }
    if (endTime == null) {
      matchBuilder.softEnding(true);
      // If it's a lunch truck, extend its time to a min of 1pm.
      if (startTime.getHourOfDay() == 10 && startTime.getMinuteOfHour() >= 30
          && truck.getCategoryList().contains("Lunch")) {
        endTime = startTime.withTime(13, 0, 0, 0);
      } else {
        endTime = startTime.plusHours(stopTime(truck, startTime));
      }
    }
    return matchBuilder
        .stop(TruckStop.builder().confidence(confidence)
            .origin(StopOrigin.TWITTER)
            .notes(notes)
            .truck(truck).startTime(startTime).endTime(endTime).location(location).build())
        .text(tweetText)
        .confidence(confidence)
        .tweetId(tweet.getId())
        .terminated(terminationTime != null)
        .build();
  }

  private boolean isMorning(DateTime time) {
    LocalTime lt = time.toLocalTime();
    // anything before 4 counts as the previous night
    return lt.isAfter(new LocalTime(4, 0)) && lt.isBefore(new LocalTime(10, 30));
  }

  /**
   * Tests for tweets like this: <code></code>THE TRUCK: M: 600 W Chicago T: NBC Tower W: Clark & Monroe
   * TH: Madison & Wacker + Montrose & Ravenswood (5pm) F: Lake & Wabash</code>
   * @return
   */
  private boolean containsAbbreviatedSchedule(String tweetText) {
    return schedulePattern.matcher(tweetText).find();
  }

  private int stopTime(Truck truck, DateTime startTime) {
    if (startTime.getHourOfDay() < 11 && truck.getCategories().contains("MorningSquatter")) {
      return Math.max(13 - startTime.getHourOfDay(), DEFAULT_STOP_LENGTH_IN_HOURS);
    }
    return truck.getCategories().contains("1HRStops") ? 1 : DEFAULT_STOP_LENGTH_IN_HOURS;
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

  private boolean verifyMatchOnlyExpression(Truck truck, String tweetText) {
    Pattern p = truck.getMatchOnlyIf();
    if (p != null) {
      Matcher m = p.matcher(tweetText.toLowerCase());
      return !m.find();
    }
    p = truck.getDonotMatchIf();
    if (p != null) {
      Matcher m = p.matcher(tweetText.toLowerCase());
      return m.find();
    }
    return false;
  }

  private boolean matchesOtherDay(String tweetText) {
    Matcher matcher = simpleDateParser.matcher(tweetText);
    if (matcher.find()) {
      LocalDate date = clock.currentDay();
      String first = matcher.group(1);
      String second = matcher.group(2);
      if (Integer.parseInt(first) != date.getMonthOfYear() || Integer.parseInt(second) != date.getDayOfMonth()) {
        return true;
      }
    }

    switch (clock.dayOfWeek()) {
      case monday:
        return monPattern.matcher(tweetText).find();
      case tuesday:
        return tuesPattern.matcher(tweetText).find();
      case wednesday:
        return wedPattern.matcher(tweetText).find();
      case thursday:
        return thursPattern.matcher(tweetText).find();
      case friday:
        return friPattern.matcher(tweetText).find();
      case saturday:
        return satPattern.matcher(tweetText).find();
      default:
        return sunPattern.matcher(tweetText).find();
    }
  }

  private @Nullable Location extractLocation(TweetSummary tweet, Truck truck) {
    List<String> addresses = addressExtractor.parse(tweet.getText(), truck);
    Location tweetLocation = tweet.getLocation();
    if (tweetLocation != null) {
      log.info("Location data enabled for tweet from " + truck.getId());
      // Currently not using this function...remove next line to re-enabled
      tweetLocation = null;
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
    if (truck.isTwitterGeolocationDataValid() && tweetLocation != null && !tweet.isReply()) {
      Location lookup = geoLocator.reverseLookup(tweetLocation);
      String name = lookup != null ? lookup.getName() : "Unnamed Location";
      return Location.builder()
          .lat(tweetLocation.getLatitude())
          .lng(tweetLocation.getLongitude())
          .name(name).build();
    }
    return null;
  }

  private @Nullable DateTime parseTime(String timeText, LocalDate date, @Nullable DateTime after) {
    int plusDay = 0;
    if (timeText.toLowerCase().equals("noon")) {
      timeText = "12:00p.m.";
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
}
