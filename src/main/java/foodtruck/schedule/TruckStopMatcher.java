package foodtruck.schedule;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
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
      "(" + TIME_PATTERN + ")\\s*-\\s*(" + TIME_PATTERN + ")[\\s|\\.&&[^\\-]]";
  private static final String TOMORROW = "2morrow|tmw|tomorrow|ma�ana";
  private final Pattern retweetPattern;
  private final AddressExtractor addressExtractor;
  private final GeoLocator geoLocator;
  private final Pattern endTimePattern;
  private final Pattern timePattern;
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

  @Inject
  public TruckStopMatcher(AddressExtractor extractor, GeoLocator geoLocator,
      DateTimeZone defaultZone, Clock clock) {
    this.addressExtractor = extractor;
    this.geoLocator = geoLocator;
    this.timePattern = Pattern.compile(TIME_PATTERN);
    this.atTimePattern = Pattern.compile("\\bat (" + TIME_PATTERN_STRICT + ")");
    this.endTimePattern = Pattern.compile("\\b(until|til|till) (" + TIME_PATTERN + ")");
    this.timeRangePattern = Pattern.compile(TIME_RANGE_PATTERN);
    this.retweetPattern = Pattern.compile("\\bRT \"?@");
    this.monPattern = Pattern.compile(
        "\\b(TUE|WED|Weds|THU|FRI|SAT|SUN|tuesday|wednesday|thursday|friday|saturday|sunday|tues|thurs|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    this.tuesPattern = Pattern.compile(
        "\\b(MON|WED|Weds|THU|FRI|SAT|SUN|monday|wednesday|thursday|friday|saturday|sunday|thurs|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    this.wedPattern = Pattern.compile(
        "\\b(MON|TUE|THU|FRI|SAT|SUN|monday|tuesday|thursday|friday|saturday|sunday|tues|thurs|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    this.thursPattern = Pattern.compile(
        "\\b(MON|TUE|WED|Weds|FRI|SAT|SUN|monday|tuesday|wednesday|friday|saturday|sunday|tues|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    this.friPattern = Pattern.compile(
        "\\b(MON|TUE|WED|Weds|THU|SAT|SUN|monday|tuesday|wednesday|thursday|saturday|sunday|tues|thurs|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    this.satPattern = Pattern.compile(
        "\\b(MON|TUE|WED|Weds|THU|FRI|SUN|monday|tuesday|wednesday|thursday|friday|sunday|tues|thurs|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    this.sunPattern = Pattern.compile(
        "\\b(MON|TUE|WED|Weds|THU|FRI|SAT|monday|tuesday|wednesday|thursday|friday|saturday|tues|thurs|" +
            TOMORROW + ")\\b",
        Pattern.CASE_INSENSITIVE);
    formatter = DateTimeFormat.forPattern("hhmma").withZone(defaultZone);
    this.clock = clock;
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
    if (isRetweet(tweetText)) {
      log.log(Level.INFO, "Didn't match '{0}' because it is a retweet",
          tweetText);
      return null;
    }

    if (verifyMatchOnlyExpression(truck, tweetText)) {
      log.log(Level.INFO, "Didn't match '{0}' because it didn't contain match-only expression",
          tweetText);
      return null;
    }
    Location location = extractLocation(tweet, truck);
    if (location == null) {
      return null;
    }
    DateTime startTime = null;
    final boolean morning = tweet.getTime().toLocalTime().isBefore(new LocalTime(10, 30));
    // TODO: this signals a schedule being tweeted, for now we can't handle that
    final String lowerCaseTweet = tweetText.toLowerCase();
    if (lowerCaseTweet.contains("stops") ||
        (morning && (lowerCaseTweet.contains("schedule")))) {
      return null;
    }

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
      endTime = parseTime(m.group(5), date, endTime);
      if (endTime != null && terminationTime != null && endTime.isAfter(terminationTime)) {
        endTime = terminationTime;
      }
    }
    // This is detecting something in the format: We will be at Merchandise mart at 11:00.
    if (startTime == null) {
      m = atTimePattern.matcher(tweetText);
      if (m.find()) {
        final LocalDate date = tweet.getTime().toLocalDate();
        startTime = parseTime(m.group(1), date, null);
        if (startTime != null) {
          if (startTime.getHourOfDay() == 0) {
            startTime = startTime.withHourOfDay(12);
          }
          endTime = startTime.plusHours(2);
        }
      }
    }
    if (startTime == null) {
      // Cupcake trucks and such should not be matched at all by this rule since they make many frequent stops
      if (!morning || truck.getCategories().contains("Breakfast")) {
        startTime = tweet.getTime();
      } else {
        startTime = tweet.getTime().withTime(11, 30, 0, 0);
        endTime = null;
      }
    }
    TruckStopMatch.Builder matchBuilder = TruckStopMatch.builder();
    if (endTime == null) {
      endTime = terminationTime == null ? parseEndTime(tweetText, startTime) : terminationTime;
    }
    if (endTime == null) {
      matchBuilder.softEnding(true);
      endTime = startTime.plusHours(DEFAULT_STOP_LENGTH_IN_HOURS);
    }
    return matchBuilder
        .stop(new TruckStop(truck, startTime, endTime, location, null, false))
        .text(tweetText)
        .terminated(terminationTime != null)
        .build();
  }

  private boolean isRetweet(String tweetText) {
    return retweetPattern.matcher(tweetText).find();
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
    for (String address : addresses) {
      Location loc = geoLocator.locate(address, GeolocationGranularity.NARROW);
      if (loc != null && loc.isResolved()) {
        return loc;
      }
    }
    return null;
  }

  private @Nullable DateTime parseTime(String timeText, LocalDate date, @Nullable DateTime after) {
    if (timeText.toLowerCase().equals("noon")) {
      timeText = "12:00p.m.";
    }
    timeText = timeText.replace(".", "");
    if (timeText.endsWith("p") || timeText.endsWith("a")) {
      timeText = timeText + "m";
    }
    timeText = timeText.replace(":", "");
    timeText = timeText.replace(" ", "");
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
      int hour = Integer.parseInt(tmpTime.substring(0, 2));
      int min = Integer.parseInt(tmpTime.substring(2, 4));
      if (after.isAfter(date.toDateTime(new LocalTime(hour, min)))) {
        suffix = "pm";
      } else {
        suffix = "am";
      }
    } else {
      int hour = Integer.parseInt(tmpTime.substring(0, 2));
      suffix = (hour > 8) ? "am" : "pm";
    }
    timeText = tmpTime + suffix;
    try {
      return formatter.parseDateTime(timeText).withDate(date.getYear(), date.getMonthOfYear(),
          date.getDayOfMonth());
    } catch (IllegalArgumentException iae) {

      log.log(Level.WARNING, iae.getMessage(), iae);
      return null;
    }
  }

  private @Nullable DateTime parseEndTime(String tweetText, DateTime startTime) {
    Matcher matcher = endTimePattern.matcher(tweetText);
    if (matcher.find()) {
      return parseTime(matcher.group(2), startTime.toLocalDate(), null);
    }
    return null;
  }
}
