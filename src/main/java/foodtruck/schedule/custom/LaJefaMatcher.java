package foodtruck.schedule.custom;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.geolocation.GeoLocator;
import foodtruck.schedule.Spot;
import foodtruck.util.Clock;
import foodtruck.util.FriendlyDateTimeFormat;

/**
 * @author aviolette
 * @since 3/29/16
 */
public class LaJefaMatcher extends MultipleTruckMatcher {
  @Inject
  public LaJefaMatcher(ImmutableList<Spot> spots, GeoLocator geoLocator,
      @FriendlyDateTimeFormat DateTimeFormatter formatter, Clock clock) {
    super("patronachicago", geoLocator, spots, formatter, clock);
  }
}
