package foodtruck.schedule;

/**
 * @author aviolette
 * @since 5/14/13
 */
public class OffTheRoadDetector {

  public boolean offTheRoad(String tweet) {
    String lower = tweet.toLowerCase();
    return lower.contains("off the road") || lower.contains("maintenance") || lower.contains("in the shop")
        || lower.contains("rain check");
  }
}
