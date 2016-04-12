package foodtruck.schedule.custom;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.geolocation.GeoLocator;
import foodtruck.schedule.Spot;

/**
 * @author aviolette
 * @since 4/9/16
 */
public class BobChaMatcher extends MultipleTruckMatcher {

  @Inject
  public BobChaMatcher(GeoLocator geoLocator, ImmutableList<Spot> commonSpots) {
    super("bobchafoodtruck", geoLocator, commonSpots);
  }
}
