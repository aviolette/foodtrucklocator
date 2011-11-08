package foodtruck.twitter;

import org.joda.time.LocalDate;

/**
 * Service for retrieving and caching tweets.
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
public interface TwitterService {
  /**
   * Puts all tweets since the last caching in the cache.
   */
  void updateTwitterCache();

  /**
   * Purges all the tweets before the specified date
   */
  void purgeTweetsBefore(LocalDate localDate);

  /**
   * Analyzes recent tweets and updates twitter-located-only trucks in the datastore
   */
  void twittalyze();
}
