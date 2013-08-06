package foodtruck.dao;

/**
 * @author aviolette
 * @since 8/5/13
 */
public interface RetweetsDAO {
  /**
   * Returns true if the truck has been retweeted since the last purge by the specified twitter handle.
   * @param truckId the truck ID
   * @param twitterHandle the twitter handle
   * @return whether it has been mentioned at that location
   */
  public boolean hasBeenRetweeted(String truckId, String twitterHandle);

  /**
   * Marks a truck as being deleted at a particular spot.
   * @param truckId the truck
   * @param twitterHandle the twitter handle
   */
  public void markRetweeted(String truckId, String twitterHandle);

  /**
   * Deletes all the current retweet information
   */
  public void deleteAll();
}
