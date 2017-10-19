package foodtruck.schedule.custom.chicago;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.AbstractSpecialMatcher;
import foodtruck.schedule.Spot;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.time.Clock;
import foodtruck.time.FriendlyDateTimeFormat;

/**
 * @author aviolette
 * @since 4/1/16
 */
public class RoostMatcher extends AbstractSpecialMatcher {
  @Inject
  public RoostMatcher(GeoLocator geoLocator, ImmutableList<Spot> commonSpots,
      @FriendlyDateTimeFormat DateTimeFormatter formatter, Clock clock) {
    super(geoLocator, commonSpots, formatter, clock);
  }

  @Override
  public void handle(TruckStopMatch.Builder builder, Story story, Truck truck) {
    if (!"theroosttruck".equals(truck.getId())) {
      return;
    }
    if (story.getText()
        .toLowerCase()
        .contains("all day") && builder.getPrimaryStop() != null && builder.getPrimaryStop()
        .getStartTime()
        .getHourOfDay() == 11 && story.getTime().getHourOfDay() < 11) {
      TruckStop stop = TruckStop.builder(builder.getPrimaryStop()).startTime(story.getTime()).build();
      builder.stop(stop);
    }
  }
}
