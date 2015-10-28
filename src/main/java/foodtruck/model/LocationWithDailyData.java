package foodtruck.model;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;

/**
 * @author aviolette
 * @since 10/27/15
 */
public class LocationWithDailyData {
  private final Location location;
  private final @Nullable Specials specials;

  public LocationWithDailyData(Location location, @Nullable Specials specials) {
    this.location = location;
    this.specials = specials;
  }

  public Location getLocation() {
    return location;
  }

  public @Nullable Specials getSpecials() {
    return specials;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("location", location)
        .add("specials", specials)
        .toString();
  }
}
