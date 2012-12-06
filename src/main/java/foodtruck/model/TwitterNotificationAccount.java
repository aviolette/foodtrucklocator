package foodtruck.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.common.base.Throwables;

import twitter4j.conf.PropertyConfiguration;

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

  private TwitterNotificationAccount(Builder builder) {
    super(builder.key);
    this.location = builder.location;
    this.oauthToken = builder.oauthToken;
    this.oauthTokenSecret = builder.oauthTokenSecret;
    this.name = builder.name;
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

  public PropertyConfiguration twitterCredentials() {
    Properties properties = new Properties();
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("twitter4j.properties");
    try {
      properties.load(in);
      in.close();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    properties.put(PropertyConfiguration.OAUTH_ACCESS_TOKEN, oauthToken);
    properties.put(PropertyConfiguration.OAUTH_ACCESS_TOKEN_SECRET, oauthTokenSecret);
    return new PropertyConfiguration(properties);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Location location;
    private String oauthToken;
    private String oauthTokenSecret;
    private long key;
    private String name;
    private String twitterHandle;

    public Builder() {}

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
