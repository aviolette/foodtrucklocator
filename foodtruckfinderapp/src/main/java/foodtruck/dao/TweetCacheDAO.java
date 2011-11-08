package foodtruck.dao;

import java.util.List;

import org.joda.time.DateTime;

import foodtruck.model.TweetSummary;

/**
 * DAO for persisting tweets.
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
public interface TweetCacheDAO {
  public List<TweetSummary> findTweetsAfter(DateTime time, String truckId);

  public void deleteBefore(DateTime dateTime);

  void setLastTweetId(long id);

  long getLastTweetId();

  void save(List<TweetSummary> tweet);
}
