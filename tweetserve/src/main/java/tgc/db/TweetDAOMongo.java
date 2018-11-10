package tgc.db;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.javadocmd.simplelatlng.LatLng;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import tgc.model.Tweet;

import java.util.List;

/**
 * @author aviolette
 * @since 11/7/12
 */
public class TweetDAOMongo implements TweetDAO {
  private DBCollection tweets;

  @Inject
  public TweetDAOMongo(DBCollection tweets) {
    this.tweets = tweets;
  }

  @Override
  public List<Tweet> findAllFromId(long tweetId) {
    DBCursor tweetList = tweets.find(new BasicDBObject("tweetId", new BasicDBObject("$gte", tweetId)));
    ImmutableList.Builder<Tweet> tweetBuilder = ImmutableList.builder();
    for (DBObject tweetObj : tweetList) {
      tweetBuilder.add(toTweet(tweetObj));
    }
    return tweetBuilder.build();
  }

  private Tweet toTweet(DBObject tweetObj) {
    Tweet.Builder builder = Tweet.builder();
    builder.tweetId((Long)tweetObj.get("tweetId"));
    builder.screenName((String) tweetObj.get("userId"));
    builder.retweet((Boolean)tweetObj.get("retweet"));
    if (tweetObj.containsField("location")) {
      DBObject locationObj = (DBObject) tweetObj.get("location");
      if (locationObj != null) {
        builder.location(new LatLng((Double)locationObj.get("lat"), (Double)locationObj.get("lng")));
      }
    }
    builder.text((String)tweetObj.get("text"));
    builder.time((Long)tweetObj.get("time"));
    return builder.build();
  }
}
