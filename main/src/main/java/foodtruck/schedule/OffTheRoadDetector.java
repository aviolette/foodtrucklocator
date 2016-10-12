package foodtruck.schedule;

/**
 * @author aviolette
 * @since 5/14/13
 */
public class OffTheRoadDetector {

  public OffTheRoadResponse offTheRoad(String tweet) {
    String lower = tweet.toLowerCase();

    if (lower.contains("off the road") || lower.contains("off the streets") || lower.contains("in the shop") ||
        lower.contains("at the shop") ||
        lower.contains("no stops today") || lower.contains("no service today") || lower.contains("no truck today") ||
        (lower.contains("cancel") && lower.contains("delivery"))) {
      return new OffTheRoadResponse(true, true);
    } else if(lower.contains("maintenance") || lower.contains("mechanic") || lower.contains("sorry") ||
        lower.contains("cancel") || lower.contains("rain check") || lower.contains("staying in")) {
      return new OffTheRoadResponse(true, false);
    }
    return new OffTheRoadResponse(false, true);
  }
}
