package foodtruck.schedule.custom;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.geolocation.GeoLocator;
import foodtruck.schedule.Spot;

/**
 * @author aviolette
 * @since 3/29/16
 */
public class LaJefaMatcher extends MultipleTruckMatcher {
  @Inject
  public LaJefaMatcher(ImmutableList<Spot> spots, GeoLocator geoLocator) {
    super("patronachicago", geoLocator, spots);
  }
}
