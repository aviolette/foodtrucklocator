package foodtruck.schedule.custom.chicago;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.geolocation.GeoLocator;
import foodtruck.schedule.MultipleTruckMatcher;
import foodtruck.schedule.Spot;
import foodtruck.time.Clock;
import foodtruck.time.FriendlyDateTimeFormat;

/**
 * @author aviolette
 * @since 3/29/16
 */
class LaJefaMatcher extends MultipleTruckMatcher {
  @Inject
  public LaJefaMatcher(ImmutableList<Spot> spots, GeoLocator geoLocator,
      @FriendlyDateTimeFormat DateTimeFormatter formatter, Clock clock) {
    super(geoLocator, spots, formatter, clock);
  }
}
