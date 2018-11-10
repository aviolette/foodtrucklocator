package tgc.model;

import com.javadocmd.simplelatlng.LatLng;

import javax.annotation.Nullable;

/**
 * @author aviolette
 * @since 11/7/12
 */
public class Tweet {
  private final String screenName;
  private final long tweetId;
  private final boolean retweet;
  private @Nullable final LatLng location;
  private final long time;
  private final String text;

  private Tweet(Builder builder) {
    this.screenName = builder.screenName;
    this.tweetId = builder.tweetId;
    this.retweet = builder.retweet;
    this.location = builder.location;
    this.time = builder.time;
    this.text = builder.text;
  }

  public String getScreenName() {
    return screenName;
  }

  public long getTweetId() {
    return tweetId;
  }

  public boolean isRetweet() {
    return retweet;
  }

  public @Nullable LatLng getLocation() {
    return location;
  }

  public long getTime() {
    return time;
  }

  public String getText() {
    return text;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String screenName;
    private long tweetId;
    private boolean retweet;
    private @Nullable LatLng location;
    private long time;
    private String text;

    public Builder screenName(String name) {
      this.screenName = name;
      return this;
    }

    public Builder tweetId(long tweetId) {
      this.tweetId = tweetId;
      return this;
    }

    public Builder retweet(boolean retweet) {
      this.retweet = retweet;
      return this;
    }

    public Builder location(@Nullable LatLng location) {
      this.location = location;
      return this;
    }

    public Builder time(long time) {
      this.time = time;
      return this;
    }

    public Builder text(String text) {
      this.text = text;
      return this;
    }

    public Tweet build() {
      return new Tweet(this);
    }
  }
}
