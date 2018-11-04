package foodtruck.model;

import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author aviolette
 * @since 10/28/18
 */
public class SlackWebhook extends ModelEntity {

  private final String locationName;
  private final String webookUrl;
  private final String accessToken;
  private final String teamId;

  private SlackWebhook(Builder builder) {
    super(builder.key);
    this.locationName = builder.locationName;
    this.webookUrl = builder.webhookUrl;
    this.accessToken = builder.accessToken;
    this.teamId = builder.teamId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(SlackWebhook webhook) {
    return new Builder(webhook);
  }

  public String getTeamId() {
    return teamId;
  }

  public String getLocationName() {
    return locationName;
  }

  public String getWebookUrl() {
    return webookUrl;
  }

  public String getAccessToken() {
    return accessToken;
  }

  @Override
  public void validate() throws IllegalStateException {
    checkState(!Strings.isNullOrEmpty(teamId), "Team ID is required");
    checkState(!Strings.isNullOrEmpty(accessToken), "Access token is required");
    checkState(!Strings.isNullOrEmpty(webookUrl), "Web hook URL is required");
    checkState(!Strings.isNullOrEmpty(locationName), "Location name is required");
  }

  public static class Builder {

    private String locationName;
    private String webhookUrl;
    private String accessToken;
    private String teamId;
    private long key;

    public Builder() {}

    public Builder(SlackWebhook webhook) {
      this.locationName = webhook.getLocationName();
      this.webhookUrl = webhook.getWebookUrl();
      this.accessToken = webhook.getAccessToken();
      this.teamId = webhook.getTeamId();
      this.key = (long) webhook.getKey();
    }

    public Builder locationName(String locationName) {
      this.locationName = locationName;
      return this;
    }

    public Builder teamId(String teamId) {
      this.teamId = teamId;
      return this;
    }

    public Builder webhookUrl(String webhookUrl) {
      this.webhookUrl = webhookUrl;
      return this;
    }

    public Builder accessToken(String accessToken) {
      this.accessToken = accessToken;
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
