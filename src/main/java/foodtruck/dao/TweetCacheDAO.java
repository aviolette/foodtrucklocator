package foodtruck.dao;

import java.util.List;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

import foodtruck.model.Story;

/**
 * DAO for persisting tweets.
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
public interface TweetCacheDAO {
  List<Story> findTweetsAfter(DateTime time, String twitterHandle,
      boolean includeIgnored);

  void deleteBefore(DateTime dateTime);

  void setLastTweetId(long id);

  long getLastTweetId();

  void save(List<Story> tweet);

  @Nullable Story findByTweetId(long id);

  void saveOrUpdate(Story summary);
}
