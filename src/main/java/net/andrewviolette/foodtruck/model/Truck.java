package net.andrewviolette.foodtruck.model;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * Static information about a food truck.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class Truck {
  private final String id;
  private final String name;
  private final String twitterHandle;
  private final String url;
  private final String iconUrl;

  public Truck(String id, String name, @Nullable String twitterHandle, @Nullable String url, String iconUrl) {
    this.id = id;
    this.name = name;
    this.twitterHandle = twitterHandle;
    this.url = url;
    this.iconUrl = iconUrl;
  }

  public String getName() {
    return name;
  }

  public String getTwitterHandle() {
    return twitterHandle;
  }

  public String getUrl() {
    return url;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public String getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, name, url, iconUrl, twitterHandle);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o == null || !(o instanceof Truck)) {
      return false;
    }
    Truck truck = (Truck) o;
    return id.equals(truck.id) && name.equals(truck.name) && iconUrl.equals(truck.iconUrl) &&
        Objects.equal(twitterHandle, truck.twitterHandle) && Objects.equal(url, truck.url); 

  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("id", id)
        .add("name", name)
        .add("url", url)
        .add("iconUrl", iconUrl)
        .add("twitterHandle", twitterHandle)
        .toString();
  }
}
