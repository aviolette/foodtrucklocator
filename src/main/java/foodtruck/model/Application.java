package foodtruck.model;

/**
 * An application that access the food truck finder
 * @author aviolette
 * @since 1/25/13
 */
public class Application extends ModelEntity {
  private final String name;
  private final String description;
  private final boolean enabled;

  public Application(Builder builder) {
    super(builder.appKey);
    this.name = builder.name;
    this.description = builder.description;
    this.enabled = builder.enabled;
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

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Application app) {
    return new Builder(app);
  }

  public static class Builder {
    private String name;
    private String appKey;
    private String description;
    private boolean enabled;

    public Builder() {}

    public Builder(Application app) {
      this.name = app.name;
      this.appKey = (String) app.getKey();
      this.description = app.description;
      this.enabled = app.enabled;
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

    public Application build() {
      return new Application(this);
    }
  }
}
