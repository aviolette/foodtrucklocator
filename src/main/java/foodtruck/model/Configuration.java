package foodtruck.model;

import com.google.appengine.api.datastore.Key;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

/**
 * @author aviolette@gmail.com
 * @since 4/10/12
 */
public class Configuration extends ModelEntity {
  private boolean yahooGeolocationEnabled;
  private boolean googleGeolocationEnabled;
  private DateTime throttleGoogleUntil;
  private boolean tweetUpdateServletEnabled;
  private Location center;
  private boolean localTwitterCachingEnabled;
  private boolean remoteTwitterCachingEnabled;
  private @Nullable String remoteTwitterCacheAddress;

  public Configuration(Object key) {
    super(key);
  }

  private Configuration(Builder builder) {
    this(builder.key);
    this.googleGeolocationEnabled = builder.googleGeolocationEnabled;
    this.yahooGeolocationEnabled = builder.yahooGeolocationEnabled;
    this.throttleGoogleUntil = builder.throttleGoogleUntil;
    this.tweetUpdateServletEnabled = builder.tweetUpdateServletEnabled;
    this.center = builder.center;
    this.localTwitterCachingEnabled = builder.localTwitterCachingEnabled;
    this.remoteTwitterCacheAddress = builder.remoteTwitterCacheAddress;
    this.remoteTwitterCachingEnabled = builder.remoteTwitterCachingEnabled;
  }

  public boolean isLocalTwitterCachingEnabled() {
    return localTwitterCachingEnabled;
  }

  public boolean isRemoteTwitterCachingEnabled() {
    return remoteTwitterCachingEnabled;
  }

  public @Nullable String getRemoteTwitterCacheAddress() {
    return remoteTwitterCacheAddress;
  }

  public boolean isYahooGeolocationEnabled() {
    return yahooGeolocationEnabled;
  }

  public boolean isGoogleGeolocationEnabled() {
    return googleGeolocationEnabled;
  }

  public boolean isTweetUpdateServletEnabled() {
    return tweetUpdateServletEnabled;
  }

  public boolean isGoogleThrottled(DateTime when) {
    return (throttleGoogleUntil != null && throttleGoogleUntil.isAfter(when));
  }

  public Location getCenter() {
    return center;
  }

  public @Nullable DateTime getThrottleGoogleUntil() {
    return throttleGoogleUntil;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Configuration config) {
    return new Builder(config);
  }


  public static class Builder {
    private boolean googleGeolocationEnabled = true;
    private boolean yahooGeolocationEnabled = false;
    private Key key;
    private @Nullable DateTime throttleGoogleUntil;
    private boolean tweetUpdateServletEnabled;
    private Location center;
    private boolean localTwitterCachingEnabled;
    private @Nullable String remoteTwitterCacheAddress;
    private boolean remoteTwitterCachingEnabled;

    public Builder() {
    }

    public Builder(Configuration config) {
      this.googleGeolocationEnabled = config.isGoogleGeolocationEnabled();
      this.yahooGeolocationEnabled = config.isYahooGeolocationEnabled();
      this.throttleGoogleUntil = config.throttleGoogleUntil;
      this.key = (Key) config.getKey();
      this.tweetUpdateServletEnabled = config.tweetUpdateServletEnabled;
      this.localTwitterCachingEnabled = config.localTwitterCachingEnabled;
      this.remoteTwitterCacheAddress = config.remoteTwitterCacheAddress;
      this.remoteTwitterCachingEnabled = config.remoteTwitterCachingEnabled;
    }

    public Builder localTwitterCachingEnabled(boolean enabled) {
      this.localTwitterCachingEnabled = enabled;
      return this;
    }

    public Builder remoteTwitterCachingEnabled(boolean enabled) {
      this.remoteTwitterCachingEnabled = enabled;
      return this;
    }

    public Builder remoteTwitterCacheAddress(@Nullable String twitterCacheAddress) {
      this.remoteTwitterCacheAddress = twitterCacheAddress;
      return this;
    }

    public Builder googleGeolocationEnabled(boolean enabled) {
      this.googleGeolocationEnabled = enabled;
      return this;
    }

    public Builder yahooGeolocationEnabled(boolean enabled) {
      this.yahooGeolocationEnabled = enabled;
      return this;
    }

    public Builder tweetUpdateServletEnabled(boolean enabled) {
      this.tweetUpdateServletEnabled = enabled;
      return this;
    }

    public Builder center(Location location) {
      this.center = location;
      return this;
    }

    public Builder key(Key key) {
      this.key = key;
      return this;
    }

    public Configuration build() {
      return new Configuration(this);
    }

    public Builder throttleGoogleGeocoding(DateTime dateTime) {
      this.throttleGoogleUntil = dateTime;
      return this;
    }
  }
}
