package foodtruck.schedule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;

/**
 * Matches a tweet to a location, truck and time.
 * @author aviolette@gmail.com
 * @since 9/22/11
 */
public class TruckStopMatcher {
  private final AddressExtractor addressExtractor;
  private final GeoLocator geoLocator;
  private final Pattern timePattern;
  private final DateTimeFormatter formatter;

  @Inject
  public TruckStopMatcher(AddressExtractor extractor, GeoLocator geoLocator, DateTimeZone defaultZone) {
    this.addressExtractor = extractor;
    this.geoLocator = geoLocator;
    this.timePattern = Pattern.compile("until (\\d+(:\\d+)*\\s*(p|pm|a|am)*)");
    formatter = DateTimeFormat.forPattern("hh a").withZone(defaultZone);
  }

  public @Nullable TruckStopMatch match(String tweetText, Truck truck, @Nullable Location tweetLocation,
      DateTime tweetTime) {
    String address = addressExtractor.parseFirst(tweetText);
    if (address == null) {
      return null;
    }
    Confidence confidence = Confidence.LOW;

    Location location = tweetLocation;
    if (location == null) {
      location = geoLocator.locate(address);
      if (location == null) {
        return null;
      }
    } else {
      confidence = Confidence.MEDIUM;
    }

    if (confidence != Confidence.MEDIUM && containsLandingStatement(tweetText)) {
      confidence = Confidence.MEDIUM;
    }

    final DateTime startTime = tweetTime;
    DateTime endTime = parseEndTime(tweetText, startTime);
    if (endTime == null) {
      endTime = startTime.plusHours(2);
    }

    confidence = Confidence.HIGH;
    return new TruckStopMatch(confidence, new TruckStop(truck, startTime, endTime, location),
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
    return Iterables.any(ImmutableList.of("landed", "we're @ ", "we are at", "we're at"),
        new Predicate<String>() {
          @Override public boolean apply(String input) {
            return lc.contains(input);
          }
        });
  }
}
