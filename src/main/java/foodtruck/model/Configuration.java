package foodtruck.model;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.Key;

import org.joda.time.DateTime;

/**
 * @author aviolette@gmail.com
 * @since 4/10/12
 */
@Deprecated // not sure if the throttling is even used that often...if so I might just rename this
public class Configuration extends ModelEntity {
  private DateTime throttleGoogleUntil;

  public Configuration(Object key) {
    super(key);
  }

  private Configuration(Builder builder) {
    this(builder.key);
    this.throttleGoogleUntil = builder.throttleGoogleUntil;
  }

  public boolean isGoogleThrottled(DateTime when) {
    return (throttleGoogleUntil != null && throttleGoogleUntil.isAfter(when));
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
    private Key key;
    private @Nullable DateTime throttleGoogleUntil;

    public Builder() {
    }

    public Builder(Configuration config) {
      this.throttleGoogleUntil = config.throttleGoogleUntil;
      this.key = (Key) config.getKey();
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
