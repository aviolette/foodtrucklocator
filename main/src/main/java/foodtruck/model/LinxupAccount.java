package foodtruck.model;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;

/**
 * @author aviolette
 * @since 10/18/16
 */
public class LinxupAccount extends ModelEntity {
  private String username;
  private String password;
  private String truckId;

  public LinxupAccount(@Nullable Long key, String username, String password, String truckId) {
    super(key);
    this.username = username;
    this.password = password;
    this.truckId = truckId;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getTruckId() {
    return truckId;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", username)
        .add("password", password)
        .add("truckId", truckId)
        .toString();
  }
}
