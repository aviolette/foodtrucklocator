package foodtruck.schedule.custom;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.geolocation.GeoLocator;
import foodtruck.schedule.Spot;

/**
 * @author aviolette
 * @since 4/12/16
 */
public class PierogiWagonMatcher extends MultipleTruckMatcher {
  @Inject
  public PierogiWagonMatcher(GeoLocator geoLocator, ImmutableList<Spot> commonSpots) {
    super("pierogiwagon", geoLocator, commonSpots);
  }
}
