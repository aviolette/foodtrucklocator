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
  private final AddressExtractor addressExtractor;
  private final GeoLocator geoLocator;
  private final Pattern timePattern;
  private final DateTimeFormatter formatter;
  private final Pattern dowPattern;
  private final Pattern futurePattern;

  @Inject
  public TruckStopMatcher(AddressExtractor extractor, GeoLocator geoLocator,
      DateTimeZone defaultZone, Clock clock) {
    this.addressExtractor = extractor;
    this.geoLocator = geoLocator;
    this.timePattern = Pattern.compile("until (\\d+(:\\d+)*\\s*(p|pm|a|am)*)");
    this.dowPattern = Pattern.compile(
        "\\b(MON|TUE|WED|THU|FRI|SAT|SUN|monday|tuesday|wednesday|thursday|friday|saturday|sunday|2morrow|tomorrow)\\b",
        Pattern.CASE_INSENSITIVE);
    this.futurePattern = Pattern.compile("going|gonna|today|2day", Pattern.CASE_INSENSITIVE);
    formatter = DateTimeFormat.forPattern("hh a").withZone(defaultZone);
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
    Location location = tweet.getLocation();
    final String tweetText = tweet.getText();
    String address = addressExtractor.parseFirst(tweetText);
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
        location = location.withName("Unnamed Location");
      } else {
        location = location.withName(address);
      }
    }
    DateTime startTime = tweet.getTime();
    final boolean morning = startTime.toLocalTime().isBefore(new LocalTime(11, 0));
    // TODO: this signals a schedule being tweeted, for now we can't handle that
    if (tweetText.toLowerCase().contains("stops") ||
        (morning && (tweetText.toLowerCase().contains("schedule")))) {
      return null;
    }

    if (dowPattern.matcher(tweetText).find()) {
      log.log(Level.FINE, "Didn't match '{0}' because it contained a day of the week", tweetText);
      return null;
    }

    // TODO: this is kind of a hack - this is a tweet announcing a future lunch location

    if (morning && futurePattern.matcher(tweetText).find()) {
      startTime = startTime.withTime(11, 30, 0, 0);
    }
    DateTime endTime =
        terminationTime == null ? parseEndTime(tweetText, startTime) : terminationTime;
    if (endTime == null) {
      endTime = startTime.plusHours(DEFAULT_STOP_LENGTH_IN_HOURS);
    }
    return new TruckStopMatch(Confidence.HIGH,
        new TruckStop(truck, startTime, endTime, location, null),
        tweetText);
  }

  private DateTime parseEndTime(String tweetText, DateTime startTime) {
    Matcher matcher = timePattern.matcher(tweetText);
    if (matcher.find()) {
      String s = matcher.group(1);
      if (s.endsWith("p") || s.endsWith("a")) {
        s = s + "m";
      }
      return formatter.parseDateTime(s).withDate(startTime.getYear(), startTime.getMonthOfYear(),
          startTime.getDayOfMonth());
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
