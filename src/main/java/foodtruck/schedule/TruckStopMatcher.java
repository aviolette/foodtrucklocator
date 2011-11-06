package foodtruck.schedule;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.geolocation.GeoLocator;
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
  public static final String TIME_PATTERN = "\\d+(:\\d+)*\\s*(p|pm|a|am)?";
  private static final String TIME_RANGE_PATTERN =
      "(" + TIME_PATTERN + ")\\s*-\\s*(" + TIME_PATTERN + ")[\\s|\\.&&[^\\-]]";
  private final AddressExtractor addressExtractor;
  private final GeoLocator geoLocator;
  private final Pattern timePattern;
  private final DateTimeFormatter formatter;
  private final Pattern dowPattern;
  private final Pattern futurePattern;
  private final Clock clock;
  private final Pattern timeRangePattern;
  private final Pattern numberMatcher;

  @Inject
  public TruckStopMatcher(AddressExtractor extractor, GeoLocator geoLocator,
      DateTimeZone defaultZone, Clock clock) {
    this.addressExtractor = extractor;
    this.geoLocator = geoLocator;
    this.numberMatcher = Pattern.compile("(\\d+)(:\\d+)?");
    this.timePattern = Pattern.compile("until (" + TIME_PATTERN + ")");
    this.timeRangePattern = Pattern.compile(TIME_RANGE_PATTERN);
    this.dowPattern = Pattern.compile(
        "\\b(MON|TUE|WED|THU|FRI|SAT|SUN|monday|tuesday|wednesday|thursday|friday|saturday|sunday|2morrow|tomorrow)\\b",
        Pattern.CASE_INSENSITIVE);
    this.futurePattern = Pattern.compile("going|gonna|today|2day", Pattern.CASE_INSENSITIVE);
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
    Location location = extractLocation(tweet, truck);
    if (location == null) {
      return null;
    }
    DateTime startTime = null;
    final boolean morning = tweet.getTime().toLocalTime().isBefore(new LocalTime(11, 0));
    // TODO: this signals a schedule being tweeted, for now we can't handle that
    if (tweetText.toLowerCase().contains("stops") ||
        (morning && (tweetText.toLowerCase().contains("schedule")))) {
      return null;
    }

    // TODO: we need to make sure that it doesn't contain the current day of the week
    // For instance: Arrived at Michigan and Walton. Come get your Sunday macaron going!
    if (dowPattern.matcher(tweetText).find()) {
      log.log(Level.INFO, "Didn't match '{0}' because it contained a day of the week", tweetText);
      return null;
    }

    // TODO: this is kind of a hack - this is a tweet announcing a future lunch location
    DateTime endTime = null;
    Matcher m = timeRangePattern.matcher(tweetText);
    if (m.find()) {
      final LocalDate date = tweet.getTime().toLocalDate();
      startTime = parseTime(m.group(1), date, null);
      endTime = parseTime(m.group(4), date, endTime);
    }
    if (startTime == null) {
      // Cupcake trucks and such should not be matched at all by this rule since they make many frequent stops
      if (!morning) {
        startTime = tweet.getTime();
      } else {
        startTime = tweet.getTime().withTime(11, 30, 0, 0);
        endTime = null;
      }
    }
    if (endTime == null) {
      endTime =
          terminationTime == null ? parseEndTime(tweetText, startTime) : terminationTime;
    }
    if (endTime == null) {
      endTime = startTime.plusHours(DEFAULT_STOP_LENGTH_IN_HOURS);
    }
    return new TruckStopMatch(Confidence.HIGH,
        new TruckStop(truck, startTime, endTime, location, null),
        tweetText);
  }

  private Location extractLocation(TweetSummary tweet, Truck truck) {
    String address = addressExtractor.parseFirst(tweet.getText(), truck);
    Location location = tweet.getLocation();
    if (address == null && location == null) {
      return null;
    }
    if (location == null) {
      location = geoLocator.locate(address);
      if (location == null) {
        return null;
      }
    } else {
      // TODO: reverse geolocation lookup
      if (address == null) {
        return location.withName("Unnamed Location");
      } else {
        return location.withName(address);
      }
    }
    return location;
  }

  private @Nullable DateTime parseTime(String timeText, LocalDate date, @Nullable DateTime after) {
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
    Matcher matcher = timePattern.matcher(tweetText);
    if (matcher.find()) {
      return parseTime(matcher.group(1), startTime.toLocalDate(), null);
    }
    return null;
  }

  private boolean containsLandingStatement(String tweetText) {
    final String lc = tweetText.toLowerCase();
    return Iterables.any(ImmutableList.of("landed", "we're @ ", "we are at", "we're at", "we're on",
        "we are on", "here at", "here on", "We moved to"),
        new Predicate<String>() {
          @Override public boolean apply(String input) {
            return lc.contains(input);
          }
        });
  }
}
