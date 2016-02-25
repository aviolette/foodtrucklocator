package foodtruck.model;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

/**
 * Represents the notifications a particular iOS device is registered for
 * @author aviolette
 * @since 2/10/16
 */
public class NotificationDeviceProfile extends ModelEntity {
  private final ImmutableList<String> truckIds;
  private final ImmutableList<String> locationNames;
  private final NotificationType notificationType;

  private NotificationDeviceProfile(Builder builder) {
    super(builder.deviceToken);
    this.truckIds = ImmutableList.copyOf(builder.truckIds);
    this.locationNames = ImmutableList.copyOf(builder.locationNames);
    this.notificationType = builder.deviceToken.contains("@") ? NotificationType.EMAIL : NotificationType.PUSH;
  }

  public String getDeviceToken() {
    return (String)getKey();
  }

  public NotificationType getType() {
    return notificationType;
  }

  public ImmutableList<String> getTruckIds() {
    return truckIds;
  }

  public ImmutableList<String> getLocationNames() {
    return locationNames;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(NotificationDeviceProfile deviceProfile) {
    return new Builder(deviceProfile);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("token", getDeviceToken())
        .add("truckIds", truckIds)
        .add("locationNames", locationNames)
        .toString();
  }

  public static class Builder {
    private String deviceToken;
    private List<String> truckIds;
    private List<String> locationNames;

    public Builder() {}

    public Builder(NotificationDeviceProfile deviceProfile) {
      this.deviceToken = deviceProfile.getDeviceToken();
      this.truckIds = deviceProfile.truckIds;
      this.locationNames = deviceProfile.locationNames;
    }

    public Builder deviceToken(String deviceToken) {
      this.deviceToken = deviceToken;
      return this;
    }

    public Builder truckIds(List<String> truckIds) {
      this.truckIds = truckIds;
      return this;
    }

    public Builder locationNames(List<String> locationNames) {
      this.locationNames = locationNames;
      return this;
    }

    public NotificationDeviceProfile build() {
      return new NotificationDeviceProfile(this);
    }
  }
}
