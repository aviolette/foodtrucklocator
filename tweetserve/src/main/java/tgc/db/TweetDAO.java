package tgc.db;

import tgc.model.Tweet;

import java.util.List;

/**
 * @author aviolette
 * @since 11/7/12
 */
public interface TweetDAO {
  /**
   * Returns all the tweets from the specified ID
   * @param tweetId the tweet ID
   * @return The list of all the tweets in ascending order chronologically
   */
  public List<Tweet> findAllFromId(long tweetId);
}
