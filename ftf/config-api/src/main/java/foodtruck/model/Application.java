package foodtruck.model;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * An application that access the food truck finder
 * @author aviolette
 * @since 1/25/13
 */
public class Application extends ModelEntity {
  private final String name;
  private final String description;
  private final boolean enabled;
  private final boolean rateLimit;
  private final boolean canHandleNotifications;

  public Application(Builder builder) {
    super(builder.appKey);
    this.name = builder.name;
    this.description = builder.description;
    this.enabled = builder.enabled;
    this.rateLimit = builder.rateLimit;
    this.canHandleNotifications = builder.canHandleNotifications;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Application app) {
    return new Builder(app);
  }

  @Override
  public void validate() throws IllegalStateException {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "Name is null");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(description), "Name is null");
  }

  public boolean isRateLimitEnabled() {
    return rateLimit;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getName() {
    return name;
  }

  public String getAppKey() {
    return (String) getKey();
  }

  public String getDescription() {
    return description;
  }

  public boolean canHandleNotifications() {
    return canHandleNotifications;
  }

  public static class Builder {
    private String name;
    private String appKey;
    private String description;
    private boolean enabled;
    private boolean rateLimit;
    private boolean canHandleNotifications;

    public Builder() {}

    public Builder(Application app) {
      this.name = app.name;
      this.appKey = (String) app.getKey();
      this.description = app.description;
      this.enabled = app.enabled;
      this.rateLimit = app.rateLimit;
      this.canHandleNotifications = app.canHandleNotifications;
    }

    public Builder rateLimit(boolean rateLimit) {
      this.rateLimit = rateLimit;
      return this;
    }

    public Builder appKey(String appKey) {
      this.appKey = appKey;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder canHandleNotifications(boolean canHandleNotifications) {
      this.canHandleNotifications = canHandleNotifications;
      return this;
    }

    public Application build() {
      return new Application(this);
    }
  }
}
