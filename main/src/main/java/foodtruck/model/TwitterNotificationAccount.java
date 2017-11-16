package foodtruck.model;

import java.util.Objects;

/**
 * @author aviolette
 * @since 12/3/12
 */
public class TwitterNotificationAccount extends ModelEntity {
  private final Location location;
  private final String oauthToken;
  private final String oauthTokenSecret;
  private final String name;
  private final String twitterHandle;
  private final boolean active;

  private TwitterNotificationAccount(Builder builder) {
    super(builder.key);
    this.location = builder.location;
    this.oauthToken = builder.oauthToken;
    this.oauthTokenSecret = builder.oauthTokenSecret;
    this.name = builder.name;
    this.active = builder.active;
    this.twitterHandle = builder.twitterHandle;
  }

  public String getTwitterHandle() {
    return twitterHandle;
  }

  public Location getLocation() {
    return location;
  }

  public String getOauthToken() {
    return oauthToken;
  }

  public String getName() {
    return name;
  }

  public String getOauthTokenSecret() {
    return oauthTokenSecret;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(TwitterNotificationAccount account) {
    return new Builder(account);
  }

  public boolean isActive() {
    return active;
  }

  @Override
  public int hashCode() {
    return Objects.hash(location, oauthToken, oauthTokenSecret, name, twitterHandle, active);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof TwitterNotificationAccount)) {
      return false;
    }
    TwitterNotificationAccount twa = (TwitterNotificationAccount) obj;
    return location.equals(twa.location) && oauthToken.equals(twa.oauthToken) &&
        oauthTokenSecret.equals(twa.oauthTokenSecret) && name.equals(twa.name) &&
        twitterHandle.equals(twa.twitterHandle) && active == twa.active;
  }

  public static class Builder {
    private Location location;
    private String oauthToken;
    private String oauthTokenSecret;
    private long key;
    private String name;
    private String twitterHandle;
    private boolean active = true;

    public Builder() {}

    public Builder(TwitterNotificationAccount account) {
      this.location = account.getLocation();
      this.oauthToken = account.getOauthToken();
      this.oauthTokenSecret = account.getOauthTokenSecret();
      this.key = (Long)account.getKey();
      this.name = account.getName();
      this.twitterHandle = account.getTwitterHandle();
      this.active = account.isActive();
    }

    public Builder key(long key) {
      this.key = key;
      return this;
    }

    public Builder location(Location location) {
      this.location = location;
      return this;
    }

    public Builder oauthToken(String token) {
      this.oauthToken = token;
      return this;
    }

    public Builder oauthTokenSecret(String secret) {
      this.oauthTokenSecret = secret;
      return this;
    }

    public TwitterNotificationAccount build() {
      return new TwitterNotificationAccount(this);
    }

    public Builder active(boolean active) {
      this.active = active;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder twitterHandle(String twitterHandle) {
      this.twitterHandle = twitterHandle;
      return this;
    }
  }
}
