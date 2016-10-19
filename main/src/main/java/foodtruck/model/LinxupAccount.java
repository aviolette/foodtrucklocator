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
  private boolean enabled;

  private LinxupAccount(Builder builder) {
    super(builder.key);
    this.username = builder.username;
    this.password = builder.password;
    this.truckId = builder.truckId;
    this.enabled = builder.enabled;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(LinxupAccount instance) {
    return new Builder(instance);
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

  public static class Builder {
    private @Nullable Long key;
    private String username;
    private String password;
    private String truckId;
    private boolean enabled = true;

    public Builder() {
    }

    public Builder(LinxupAccount instance) {
      this.key = (Long) instance.getKey();
      this.username = instance.username;
      this.password = instance.password;
      this.truckId = instance.truckId;
      this.enabled = instance.enabled;
    }

    public Builder key(Long key) {
      this.key = key;
      return this;
    }

    public Builder username(String username) {
      this.username = username;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public Builder truckId(String truckId) {
      this.truckId = truckId;
      return this;
    }

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public LinxupAccount build() {
      return new LinxupAccount(this);
    }
  }
}
