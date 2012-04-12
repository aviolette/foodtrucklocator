// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.model;

import com.google.appengine.api.datastore.Key;

/**
 * @author aviolette@gmail.com
 * @since 4/10/12
 */
public class Configuration extends ModelEntity {
  private boolean yahooGeolocationEnabled;
  private boolean googleGeolocationEnabled;

  public Configuration(Object key) {
    super(key);
  }

  private Configuration(Builder builder) {
    this(builder.key);
    this.googleGeolocationEnabled = builder.googleGeolocationEnabled;
    this.yahooGeolocationEnabled = builder.yahooGeolocationEnabled;
  }

  public boolean isYahooGeolocationEnabled() {
    return yahooGeolocationEnabled;
  }

  public boolean isGoogleGeolocationEnabled() {
    return googleGeolocationEnabled;
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

    public Builder() {
    }

    public Builder(Configuration config) {
      this.googleGeolocationEnabled = config.isGoogleGeolocationEnabled();
      this.yahooGeolocationEnabled = config.isYahooGeolocationEnabled();
      this.key = (Key) config.getKey();
    }

    public Builder googleGeolocationEnabled(boolean enabled) {
      this.googleGeolocationEnabled = enabled;
      return this;
    }

    public Builder yahooGeolocationEnabled(boolean enabled) {
      this.yahooGeolocationEnabled = enabled;
      return this;
    }

    public Builder key(Key key) {
      this.key = key;
      return this;
    }

    public Configuration build() {
      return new Configuration(this);
    }
  }
}
