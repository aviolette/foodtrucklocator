package foodtruck.schedule;

import com.google.common.collect.ImmutableList;

import foodtruck.geolocation.GeoLocator;

/**
 * @author aviolette
 * @since 3/29/16
 */
public abstract class AbstractSpecialMatcher implements SpecialMatcher {
  private final GeoLocator geoLocator;
  private final ImmutableList<Spot> commonSpots;

  AbstractSpecialMatcher(GeoLocator geoLocator, ImmutableList<Spot> commonSpots) {
    this.geoLocator = geoLocator;
    this.commonSpots = commonSpots;
  }

  public GeoLocator getGeoLocator() {
    return geoLocator;
  }

  public ImmutableList<Spot> getCommonSpots() {
    return commonSpots;
  }
}
