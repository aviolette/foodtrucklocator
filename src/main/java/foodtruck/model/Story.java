package foodtruck.model;

import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

import org.joda.time.DateTime;

/**
 * A subset of the twitter data that this app needs.
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
public class Story extends ModelEntity {
  private static final Pattern retweetPattern = Pattern.compile("(\\bRT \"?)|(\")@");
  private final String screenName;
  private final Location location;
  private final String text;
  private final DateTime time;
  private final long id;
  private final boolean ignoreInTwittalyzer;
  private final StoryType storyType;

  public Story(Builder builder) {
    super(builder.key);
    this.text = builder.text;
    this.location = builder.location;
    this.time = builder.time;
    this.screenName = builder.screenName;
    this.id = builder.id;
    this.ignoreInTwittalyzer = builder.ignoreInTwittalyzer;
    this.storyType = builder.type;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Story story) {
    return new Builder(story);
  }

  public String getScreenName() {
    return screenName;
  }

  public @Nullable Location getLocation() {
    return location;
  }

  public String getSanitizedText() {
    return text.replaceAll(" ", " ");
  }

  public String getText() {
    return text;
  }

  public StoryType getStoryType() {
    return storyType;
  }

  public DateTime getTime() {
    return time;
  }

  public long getId() {
    return id;
  }

  public boolean isManualRetweet() {
    return retweetPattern.matcher(text).find();
  }

  public boolean isReply() {
    return !(Strings.isNullOrEmpty(text) || text.toLowerCase().startsWith("@" + screenName.toLowerCase() + " ")) && text
        .charAt(0) == '@';
  }

  @Override public int hashCode() {
    return Objects.hashCode(screenName, id, location, text, time);
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o == null || !(o instanceof Story)) {
      return false;
    }
    Story that = (Story) o;
    return id == that.id && text.equals(that.text) && Objects.equal(location, that.location) &&
        screenName.equals(that.screenName);
  }

  @Override public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("screenName", screenName)
        .add("text", text)
        .add("location", location)
        .add("id", id)
        .add("time", time)
        .add("ignore", ignoreInTwittalyzer)
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
    private StoryType type = StoryType.TWEET;

    public Builder() {
    }

    public Builder(Story summary) {
      this.screenName = summary.screenName;
      this.location = summary.location;
      this.time = summary.time;
      this.text = summary.text;
      this.id = summary.id;
      this.ignoreInTwittalyzer = summary.ignoreInTwittalyzer;
      this.key = summary.key;
      this.type = summary.storyType;
    }

    public Story build() {
      return new Story(this);
    }

    public Builder type(StoryType storyType) {
      this.type = storyType;
      return this;
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
