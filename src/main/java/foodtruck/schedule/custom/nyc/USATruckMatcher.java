package foodtruck.schedule.custom.nyc;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.geolocation.GeoLocator;
import foodtruck.schedule.MultipleTruckMatcher;
import foodtruck.schedule.Spot;
import foodtruck.util.Clock;
import foodtruck.util.FriendlyDateTimeFormat;

/**
 * @author aviolette
 * @since 10/6/16
 */
public class USATruckMatcher extends MultipleTruckMatcher {

  @Inject
  public USATruckMatcher(GeoLocator geoLocator, ImmutableList<Spot> commonSpots,
      @FriendlyDateTimeFormat DateTimeFormatter formatter, Clock clock) {
    super(geoLocator, commonSpots, formatter, clock);
  }
}
