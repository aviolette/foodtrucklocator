package foodtruck.schedule;

import foodtruck.model.TruckStop;

/**
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
}
