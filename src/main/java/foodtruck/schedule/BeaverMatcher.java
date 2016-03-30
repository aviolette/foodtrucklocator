package foodtruck.schedule;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.StopOrigin;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;

/**
 * @author aviolette
 * @since 3/29/16
 */
public class BeaverMatcher extends AbstractSpecialMatcher {

  @Inject
  public BeaverMatcher(GeoLocator geoLocator, ImmutableList<Spot> commonSpots) {
    super(geoLocator, commonSpots);
  }

  @Override
  public void handle(TruckStopMatch.Builder builder, Story story, Truck truck) {
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
          .location(getGeoLocator().locate("Madison and Wacker, Chicago, IL", GeolocationGranularity.NARROW))
          .build());
    }
    for (Spot spot : getCommonSpots()) {
      if (spot.contains(stripped)) {
        stops.add(TruckStop.builder()
            .startTime(story.getTime().withTime(7, 0, 0, 0))
            .endTime(story.getTime().withTime(14, 0, 0, 0))
            .origin(StopOrigin.TWITTER)
            .truck(truck)
            .locked(true)
            .location(getGeoLocator().locate(spot.getCanonicalForm(), GeolocationGranularity.NARROW))
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
          .location(getGeoLocator().locate("Southport and Addison, Chicago, IL", GeolocationGranularity.NARROW))
          .build());
      stops.add(TruckStop.builder()
          .endTime(story.getTime().withTime(13, 0, 0, 0))
          .startTime(story.getTime().withTime(8, 0, 0, 0))
          .truck(truck)
          .locked(true)
          .location(getGeoLocator().locate("Sangamon and Monroe, Chicago, IL", GeolocationGranularity.NARROW))
          .build());
    }
    ImmutableList<TruckStop> truckStops = stops.build();
    if (truckStops.size() == 1 && !truckStops.get(0).getLocation().getName().contains("Wacker") && lowerTweet.contains("wacker")) {
      stops.add(TruckStop.builder()
          .startTime(story.getTime().withTime(7, 0, 0, 0))
          .endTime(story.getTime().withTime(14, 0, 0, 0))
          .origin(StopOrigin.TWITTER)
          .truck(truck)
          .locked(true)
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
