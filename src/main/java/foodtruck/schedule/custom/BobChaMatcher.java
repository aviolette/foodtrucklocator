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
 * @since 4/9/16
 */
public class BobChaMatcher extends MultipleTruckMatcher {

  @Inject
  public BobChaMatcher(GeoLocator geoLocator, ImmutableList<Spot> commonSpots, @FriendlyDateTimeFormat DateTimeFormatter formatter, Clock clock) {
    super("bobchafoodtruck", geoLocator, commonSpots, formatter, clock);
  }
}
