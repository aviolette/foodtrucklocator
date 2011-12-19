package foodtruck.twitter;

import java.util.List;

import foodtruck.model.TweetSummary;

/**
 * Updates a tweet cache.
 * @author aviolette@gmail.com
 * @since 10/26/11
 */
public interface TweetCacheUpdater {
  void update(List<TweetSummary> summaries);
}
