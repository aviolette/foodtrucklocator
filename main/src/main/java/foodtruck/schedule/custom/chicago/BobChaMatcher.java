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
 * @since 4/9/16
 */
public class BobChaMatcher extends MultipleTruckMatcher {

  @Inject
  public BobChaMatcher(GeoLocator geoLocator, ImmutableList<Spot> commonSpots, @FriendlyDateTimeFormat DateTimeFormatter formatter, Clock clock) {
    super(geoLocator, commonSpots, formatter, clock);
  }
}
