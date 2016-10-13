package foodtruck.model;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;

/**
 * @author aviolette
 * @since 10/27/15
 */
public class LocationWithDailyData {
  private final Location location;
  private final @Nullable DailyData dailyData;

  public LocationWithDailyData(Location location, @Nullable DailyData dailyData) {
    this.location = location;
    this.dailyData = dailyData;
  }

  public Location getLocation() {
    return location;
  }

  public @Nullable
  DailyData getDailyData() {
    return dailyData;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("location", location)
        .add("daily data", dailyData)
        .toString();
  }
}
