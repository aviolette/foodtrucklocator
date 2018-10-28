package foodtruck.model;

/**
 * @author aviolette
 * @since 10/28/18
 */
public class SlackWebhook extends ModelEntity {

  private final String locationName;
  private final String webookUrl;

  private SlackWebhook(Builder builder) {
    super(builder.key);
    this.locationName = builder.locationName;
    this.webookUrl = builder.webhookUrl;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getLocationName() {
    return locationName;
  }

  public String getWebookUrl() {
    return webookUrl;
  }

  public static class Builder {

    private String locationName;
    private String webhookUrl;
    private long key;

    public Builder() {}

    public Builder locationName(String locationName) {
      this.locationName = locationName;
      return this;
    }

    public Builder webhookUrl(String webhookUrl) {
      this.webhookUrl = webhookUrl;
      return this;
    }

    public SlackWebhook build() {
      return new SlackWebhook(this);
    }

    public Builder key(long id) {
      this.key = id;
      return this;
    }
  }

}
