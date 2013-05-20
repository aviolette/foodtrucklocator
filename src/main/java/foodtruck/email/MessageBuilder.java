package foodtruck.email;

import java.text.MessageFormat;

import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TweetSummary;

/**
 * @author aviolette
 * @since 5/20/13
 */
public class MessageBuilder {
  public String locationAddedMessage(Location location, TweetSummary tweet, Truck truck) {
    return MessageFormat.format("This tweet \"{0}\" triggered the following location to be added {1}.  Click here to " +
        "view the location http://www.chicagofoodtruckfinder.com/admin/locations/{2} .  " +
        "Also, view the truck here: http://www.chicagofoodtruckfinder.com/admin/trucks/{3}", tweet.getText(),
        location.getName(), String.valueOf(location.getKey()), truck.getId());
  }
}
