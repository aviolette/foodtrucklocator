package foodtruck.schedule;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import foodtruck.model.Story;
import foodtruck.model.TruckStop;

/**
 * Represents a matching of a truck to a tweet.
 *
 * @author aviolette@gmail.com
 * @since 9/19/11
 */
public class TruckStopMatch {
  private final TruckStop stop;
  private final boolean softEnding;
  private final Story story;
  private final ImmutableList<TruckStop> additionalStops;

  private TruckStopMatch(Builder builder) {
    this.stop = builder.getPrimaryStop();
    this.softEnding = builder.softEnding;
    this.story = builder.story;
    this.additionalStops = builder.getAdditionalStops();
  }

  public static Builder builder() {
    return new Builder();
  }

  public ImmutableList<TruckStop> getAdditionalStops() {
    return additionalStops;
  }

  public TruckStop getStop() {
    return stop;
  }

  /**
   * Returns true if the end-point was defaulted instead of using information in the tweet
   */
  public boolean isSoftEnding() {
    return softEnding;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("stop", stop).add("text", story.getText()).toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(stop, story.getText());
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o == null || !(o instanceof TruckStopMatch)) {
      return false;
    }
    TruckStopMatch match = (TruckStopMatch) o;
    return stop.equals(match.stop) && story.getText().equals(match.story.getText());
  }

  public Story getStory() {
    return story;
  }

  public static class Builder {
    public List<TruckStop> stops = Lists.newLinkedList();
    private boolean softEnding;
    private Story story;

    public Builder() {
    }

    public Builder story(Story story) {
      this.story = story;
      return this;
    }

    public Builder stop(TruckStop stop) {
      stops.clear();
      stops.add(stop);
      return this;
    }

    public Builder addAll(ImmutableList<TruckStop> stops) {
      this.stops.clear();
      this.stops.addAll(stops);
      return this;
    }

    public Builder appendStop(TruckStop stop) {
      stops.add(stop);
      return this;
    }

    public Builder stops(ImmutableList<TruckStop> stops) {
      this.stops = Lists.newLinkedList(stops);
      return this;
    }

    public Builder softEnding(boolean softEnding) {
      this.softEnding = softEnding;
      return this;
    }

    public TruckStopMatch build() {
      return new TruckStopMatch(this);
    }

    @Nullable
    public TruckStop getPrimaryStop() {
      if (stops.isEmpty()) {
        return null;
      }
      return stops.get(0);
    }

    ImmutableList<TruckStop> getAdditionalStops() {
      if (stops.size() < 2) {
        return ImmutableList.of();
      }
      return ImmutableList.copyOf(stops.subList(1, stops.size()));
    }
  }
}
