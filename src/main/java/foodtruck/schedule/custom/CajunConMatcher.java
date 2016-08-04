package foodtruck.schedule.custom;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.schedule.AbstractSpecialMatcher;
import foodtruck.schedule.Spot;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.util.Clock;
import foodtruck.util.FriendlyDateTimeFormat;

/**
 * @author aviolette
 * @since 5/11/16
 */
public class CajunConMatcher extends AbstractSpecialMatcher {

  @Inject
  public CajunConMatcher(GeoLocator geoLocator, ImmutableList<Spot> commonSpots,
      @FriendlyDateTimeFormat DateTimeFormatter formatter, Clock clock) {
    super(geoLocator, commonSpots, formatter, clock);
  }

  @Override
  public void handle(TruckStopMatch.Builder builder, Story story, Truck truck) {
    if (!"thecajuncon".equals(truck.getId())) {
      return;
    }

    if (story.getText().contains("27th California") && story.getText().contains("11th Hamilton") &&
        story.getTime().getHourOfDay() < 9) {
      builder.appendStop(truckStop(story, truck)
          .location(getGeoLocator().locate("1100 South Hamilton, Chicago, IL", GeolocationGranularity.NARROW))
          .startTime(story.getTime().withTime(10, 30, 0, 0))
          .endTime(story.getTime().withTime(13, 30, 0, 0))
          .build());
    }
  }
}
