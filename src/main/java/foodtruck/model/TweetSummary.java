package foodtruck.model;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

import org.joda.time.DateTime;

/**
 * A subset of the twitter data that this app needs.
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
public class TweetSummary extends ModelEntity {
  private final String screenName;
  private final Location location;
  private final String text;
  private final DateTime time;
  private final long id;
  private final boolean ignoreInTwittalyzer;

  public TweetSummary(Builder builder) {
    super(builder.key);
    this.text = builder.text;
    this.location = builder.location;
    this.time = builder.time;
    this.screenName = builder.screenName;
    this.id = builder.id;
    this.ignoreInTwittalyzer = builder.ignoreInTwittalyzer;
  }

  public String getScreenName() {
    return screenName;
  }

  public @Nullable Location getLocation() {
    return location;
  }

  public String getText() {
    return text;
  }

  public DateTime getTime() {
    return time;
  }

  public long getId() {
    return id;
  }

  @Override public int hashCode() {
    return Objects.hashCode(screenName, id, location, text, time);
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o == null || !(o instanceof TweetSummary)) {
      return false;
    }
    TweetSummary that = (TweetSummary) o;
    return id == that.id && text.equals(that.text) && Objects.equal(location, that.location) &&
        screenName.equals(that.screenName);
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("screenName", screenName)
        .add("text", text)
        .add("location", location)
        .add("id", id)
        .add("time", time)
        .toString();
  }

  public boolean getIgnoreInTwittalyzer() {
    return ignoreInTwittalyzer;
  }

  public static class Builder {
    private String screenName;
    private Location location;
    private DateTime time;
    private String text;
    private long id;
    private boolean ignoreInTwittalyzer;
    private Object key;

    public Builder() {
    }

    public Builder(TweetSummary summary) {
      this.screenName = summary.screenName;
      this.location = summary.location;
      this.time = summary.time;
      this.text = summary.text;
      this.id = summary.id;
      this.ignoreInTwittalyzer = summary.ignoreInTwittalyzer;
      this.key = summary.key;
    }

    public TweetSummary build() {
      return new TweetSummary(this);
    }

    public Builder text(String text) {
      this.text = text;
      return this;
    }

    public Builder time(DateTime time) {
      this.time = time;
      return this;
    }

    public Builder location(@Nullable Location location) {
      this.location = location;
      return this;
    }

    public Builder id(long id) {
      this.id = id;
      return this;
    }

    public Builder key(@Nullable Object key) {
      this.key = key;
      return this;
    }

    public Builder ignoreInTwittalyzer(boolean ignore) {
      this.ignoreInTwittalyzer = ignore;
      return this;
    }

    public Builder userId(String screenName) {
      this.screenName = screenName;
      return this;
    }
  }
}
