package foodtruck.schedule.custom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.AbstractSpecialMatcher;
import foodtruck.schedule.Spot;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.util.Clock;
import foodtruck.util.FriendlyDateTimeFormat;

/**
 * @author aviolette
 * @since 4/10/16
 */
public class AmanecerTacosMatcher extends AbstractSpecialMatcher {
  private final Pattern matchPattern = Pattern.compile("8(.*)9(.*)10(.*)");

  @Inject
  public AmanecerTacosMatcher(GeoLocator geoLocator, @FriendlyDateTimeFormat DateTimeFormatter formatter, Clock clock) {
    super(geoLocator, ImmutableList.of(
        new Spot("davis", "Davis Station"),
        new Spot("foster/sheridan", "Foster and Sheridan, Evanston, IL"),
        new Spot("sheridan/noyes", "Sheridan and Noyes, Evanston, IL"),
        new Spot("sheridan/garrett", "Garrett and Sheridan, Evanston, IL"),
        new Spot("foster/garrett", "Foster and Garrett, Evanston, IL"),
        new Spot("davis station", "Davis Station")), formatter, clock);
  }

  @Override
  public void handle(TruckStopMatch.Builder builder, Story story, Truck truck) {
    if (!"amanecertacos".equals(truck.getId())) {
      return;
    }
    String lowerTweet = story.getText().toLowerCase();
    String stripped = lowerTweet.replace(" ", "");
    stripped = stripped.replace("\n", "");
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    Matcher m = matchPattern.matcher(stripped);
    if (m.find()) {
      matchAt(story.getTime().withTime(8, 0, 0, 0), story.getTime().withTime(8, 45, 0, 0), m.group(1), stops, truck, story);
      matchAt(story.getTime().withTime(9, 0, 0, 0), story.getTime().withTime(9, 45, 0, 0), m.group(2), stops, truck, story);
      matchAt(story.getTime().withTime(10, 0, 0, 0), story.getTime().withTime(10, 45, 0, 0), m.group(3), stops, truck, story);
    }
    ImmutableList<TruckStop> allStops = stops.build();
    if (!allStops.isEmpty()) {
      builder.addAll(allStops);
    }
  }

  private void matchAt(DateTime startTime, DateTime endTime, String text, ImmutableList.Builder<TruckStop> stops,
      Truck truck, Story story) {
    Location location = findMatch(text);
    if (location == null) {
      return;
    }
    stops.add(truckStop(story, truck)
        .location(location)
        .startTime(startTime)
        .endTime(endTime)
        .build());
  }

  private @Nullable Location findMatch(String text) {
    for (Spot spot : getCommonSpots()) {
      if (spot.contains(text)) {
        Location location = getGeoLocator().locate(spot.getCanonicalForm(), GeolocationGranularity.NARROW);
        if (location != null) {
          return location;
        }
      }
    }
    return null;
  }
}
