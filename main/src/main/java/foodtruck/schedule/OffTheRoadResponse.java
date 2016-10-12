package foodtruck.schedule;

/**
 * @author aviolette
 * @since 3/7/14
 */
public class OffTheRoadResponse {
  private boolean offTheRoad, confidenceHigh;

  public OffTheRoadResponse(boolean offTheRoad, boolean confidenceHigh) {
    this.offTheRoad = offTheRoad;
    this.confidenceHigh = confidenceHigh;
  }

  public boolean isOffTheRoad() {
    return offTheRoad;
  }

  public boolean isConfidenceHigh() {
    return confidenceHigh;
  }
}
