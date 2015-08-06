package foodtruck.socialmedia;

import org.joda.time.LocalDate;

/**
 * Service for caching social media data for the purposes of periodically generated into stops.
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
public interface SocialMediaCacher {

  /**
   * Syncs social media data to local cache
   */
  void update();

  /**
   * Purges all the social media data before the specified date
   */
  void purgeBefore(LocalDate localDate);

  /**
   * Analyzes recent social media data from trucks and updates twitter-located-only trucks in the datastore
   */
  void analyze();
}
