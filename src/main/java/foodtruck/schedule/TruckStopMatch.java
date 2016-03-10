package foodtruck.schedule;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import foodtruck.model.TruckStop;

/**
 * Represents a matching of a truck to a tweet.
 * @author aviolette@gmail.com
 * @since 9/19/11
 */
public class TruckStopMatch {
  private final Confidence confidence;
  private final TruckStop stop;
  private final String text;
  private final boolean terminated;
  private final boolean softEnding;
  private final long tweetId;
  private final ImmutableList<TruckStop> additionalStops;

  private TruckStopMatch(Builder builder) {
    this.confidence = builder.confidence;
    this.stop = builder.stops.get(0);
    this.text = builder.text;
    this.terminated = builder.terminated;
    this.softEnding = builder.softEnding;
    this.tweetId = builder.tweetId;
    this.additionalStops = builder.getAdditionalStops();
  }

  public ImmutableList<TruckStop> getAdditionalStops() {
    return additionalStops;
  }

  public boolean isTerminated() {
    return terminated;
  }

  public Confidence getConfidence() {
    return confidence;
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

  public String getText() {
    return text;
  }

  @Override public String toString() {
    return MoreObjects.toStringHelper(this).add("confidence", confidence).add("stop", stop)
        .add("text", text).toString();
  }

  @Override public int hashCode() {
    return Objects.hashCode(confidence, stop, text);
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o == null || !(o instanceof TruckStopMatch)) {
      return false;
    }
    TruckStopMatch match = (TruckStopMatch) o;
    return confidence == match.confidence && stop.equals(match.stop) && text.equals(match.text);
  }

  public static Builder builder() {
    return new Builder();
  }

  public long getTweetId() {
    return tweetId;
  }

  public static class Builder {
    private String text;
    private Confidence confidence = Confidence.HIGH;
    private boolean terminated;
    private boolean softEnding;
    private long tweetId;
    public List<TruckStop> stops = Lists.newLinkedList();

    public Builder() {
    }

    public Builder tweetId(long tweetId) {
      this.tweetId = tweetId;
      return this;
    }

    public Builder stop(TruckStop stop) {
      stops.clear();
      stops.add(stop);
      return this;
    }

    public Builder text(String text) {
      this.text = text;
      return this;
    }

    public Builder stops(ImmutableList<TruckStop> stops) {
      this.stops = stops;
      return this;
    }

    public Builder confidence(Confidence confidence) {
      this.confidence = confidence;
      return this;
    }

    public Builder softEnding(boolean softEnding) {
      this.softEnding = softEnding;
      return this;
    }

    public Builder terminated(boolean terminated) {
      this.terminated = terminated;
      return this;
    }

    public TruckStopMatch build() {
      return new TruckStopMatch(this);
    }

    public ImmutableList<TruckStop> getAdditionalStops() {
      if (stops.size() < 2) {
        return ImmutableList.of();
      }
      return ImmutableList.copyOf(stops.subList(1, stops.size()));
    }
  }
}
