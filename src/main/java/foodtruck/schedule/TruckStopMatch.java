package foodtruck.schedule;

import com.google.common.base.Objects;

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

  private TruckStopMatch(Builder builder) {
    this.confidence = builder.confidence;
    this.stop = builder.stop;
    this.text = builder.text;
    this.terminated = builder.terminated;
    this.softEnding = builder.softEnding;
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
    return Objects.toStringHelper(this).add("confidence", confidence).add("stop", stop)
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

  public static class Builder {
    private TruckStop stop;
    private String text;
    private Confidence confidence = Confidence.HIGH;
    private boolean terminated;
    private boolean softEnding;

    public Builder() {
    }

    public Builder stop(TruckStop stop) {
      this.stop = stop;
      return this;
    }

    public Builder text(String text) {
      this.text = text;
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
  }
}
