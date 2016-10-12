package foodtruck.schedule;

import java.util.List;

import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public interface AddressExtractor {
  /**
   * Returns the list of addresses that were matched in the tweet.
   */
  List<String> parse(String tweet, Truck truck);
}
