package foodtruck.schedule;

import com.google.common.collect.ImmutableList;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.DayOfWeek;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 4/12/16
 */
public abstract class MultipleTruckMatcher extends AbstractSpecialMatcher {

  public MultipleTruckMatcher(GeoLocator geoLocator, ImmutableList<Spot> commonSpots, DateTimeFormatter formatter,
      Clock clock) {
    super(geoLocator, commonSpots, formatter, clock);
  }

  @Override
  public void handle(TruckStopMatch.Builder builder, Story story, Truck truck) {
    DayOfWeek dayOfWeek = DayOfWeek.fromConstant(story.getTime().getDayOfWeek());
    //noinspection ConstantConditions
    if (dayOfWeek.isWeekend() || story.getTime().getHourOfDay() > 13) {
      return;
    }
    String lowerTweet = story.getText().toLowerCase();
    String stripped = lowerTweet.replace(" ", "");
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    for (Spot spot : getCommonSpots()) {
      if (spot.contains(stripped)) {
        stops.add(truckStop(story, truck)
            .startTime(story.getTime().withTime(11, 0, 0, 0))
            .endTime(story.getTime().withTime(14, 0, 0, 0))
            .location(getGeoLocator().locate(spot.getCanonicalForm(), GeolocationGranularity.NARROW))
            .build());
        if ("University of Chicago".equals(spot.getCanonicalForm())) {
          // multiple forms match UofC and they are at the end.
          // kind of a hack
          break;
        }
      }
    }
    ImmutableList<TruckStop> allStops = stops.build();
    if (allStops.size() > 1) {
      builder.addAll(allStops);
    }
  }
}
