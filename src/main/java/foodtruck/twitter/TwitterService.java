package foodtruck.twitter;

import foodtruck.model.TweetSummary;
import org.joda.time.LocalDate;

import javax.annotation.Nullable;
import java.util.List;

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

  /**
   * Finds all the tweets for a truck (NOT VERY PRACTICAL WHEN WE STORE MORE THAN A DAY, BUT NOW WE"RE
   * PURGING AFTER ONE DAY)
   * @param truckId the truckId
   * @return a list of tweet summaries
   */
  List<TweetSummary> findByTwitterHandle(String truckId);

  /**
   * Finds a summary by its id
   * @param id the tweet Id
   * @return the tweet or null if it could not be found
   */
  @Nullable TweetSummary findByTweetId(long id);

  void save(TweetSummary summary);

  /**
   * Puts tweets in the local cache from a remote cache.
   */
  void updateFromRemoteCache();
}
