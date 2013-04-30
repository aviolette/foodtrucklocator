package foodtruck.email;

import foodtruck.model.Truck;
import foodtruck.model.TweetSummary;

/**
 * @author aviolette
 * @since 4/29/13
 */
public interface EmailNotifier {
  public void systemNotifyOffTheRoad(Truck truck, TweetSummary summary);
}
