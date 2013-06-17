package foodtruck.model;

import java.util.List;

import com.google.common.base.Objects;

/**
 * This is someone who reports that trucks are at a specific spot
 * @author aviolette
 * @since 5/17/13
 */
public class TruckObserver extends ModelEntity {
  private final String twitterHandle;
  private final Location location;
  private final List<String> keywords;

  public TruckObserver(String twitterHandle, Location location, List<String> keywords) {
    super(twitterHandle);
    this.location = location;
    this.twitterHandle = twitterHandle;
    this.keywords = keywords;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  @Override public int hashCode() {
    return Objects.hashCode(location, twitterHandle);
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof TruckObserver)) {
      return false;
    }
    TruckObserver that = (TruckObserver) o;
    return location.equals(that.location) && twitterHandle.equals(that.twitterHandle);
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("location", location)
        .add("twitter handle", twitterHandle)
        .toString();
  }

  public String getTwitterHandle() {
    return twitterHandle;
  }

  public Location getLocation() {
    return location;
  }
}
