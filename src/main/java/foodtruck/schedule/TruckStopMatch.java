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

  public TruckStopMatch(Confidence confidence, TruckStop stop, String text) {
    this.confidence = confidence;
    this.stop = stop;
    this.text = text;
  }

  public Confidence getConfidence() {
    return confidence;
  }

  public TruckStop getStop() {
    return stop;
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
}
