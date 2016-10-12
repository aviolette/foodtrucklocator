package foodtruck.schedule.custom.chicago;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
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
 * @since 3/29/16
 */
public class BeaverMatcher extends AbstractSpecialMatcher {
  @Inject
  public BeaverMatcher(GeoLocator geoLocator, ImmutableList<Spot> commonSpots,
      @FriendlyDateTimeFormat DateTimeFormatter formatter, Clock clock) {
    super(geoLocator, commonSpots, formatter, clock);
  }

  @Override
  public void handle(TruckStopMatch.Builder builder, Story story, Truck truck) {
    String lowerTweet = story.getText().toLowerCase();
    String stripped = lowerTweet.replace(" ", "");
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    if (lowerTweet.contains("wacker") && lowerTweet.contains("madison")) {
      stops.add(truckStop(story, truck)
          .startTime(story.getTime().withTime(7, 0, 0, 0))
          .endTime(story.getTime().withTime(10, 0, 0, 0))
          .location(getGeoLocator().locate("Madison and Wacker, Chicago, IL", GeolocationGranularity.NARROW))
          .build());
    }
    for (Spot spot : getCommonSpots()) {
      if (spot.contains(stripped)) {
        stops.add(truckStop(story, truck)
            .startTime(story.getTime().withTime(7, 0, 0, 0))
            .endTime(story.getTime().withTime(18, 0, 0, 0))
            .location(getGeoLocator().locate(spot.getCanonicalForm(), GeolocationGranularity.NARROW))
            .build());
      }
    }
    if (lowerTweet.contains("sangamon") && lowerTweet.contains("southport") && story.getTime().getHourOfDay() < 13) {
      builder.softEnding(false);
      stops.add(truckStop(story, truck)
          .endTime(story.getTime().withTime(14, 0, 0, 0))
          .startTime(story.getTime().withTime(8, 0, 0, 0))
          .location(getGeoLocator().locate("Southport and Addison, Chicago, IL", GeolocationGranularity.NARROW))
          .build());
      stops.add(truckStop(story, truck)
          .endTime(story.getTime().withTime(13, 0, 0, 0))
          .startTime(story.getTime().withTime(8, 0, 0, 0))
          .location(getGeoLocator().locate("Sangamon and Monroe, Chicago, IL", GeolocationGranularity.NARROW))
          .build());
    }
    ImmutableList<TruckStop> truckStops = stops.build();
    if (truckStops.size() == 1 && !truckStops.get(0).getLocation().getName().contains("Wacker") && lowerTweet.contains("wacker")) {
      stops.add(truckStop(story, truck)
          .startTime(story.getTime().withTime(7, 0, 0, 0))
          .endTime(story.getTime().withTime(18, 0, 0, 0))
          .location(getGeoLocator().locate("Wacker and Adams, Chicago, IL", GeolocationGranularity.NARROW))
          .build());
      truckStops = stops.build();
    }
    if (truckStops.size() > 1) {
      builder.softEnding(false);
      builder.stops(truckStops);
    }
  }
}
