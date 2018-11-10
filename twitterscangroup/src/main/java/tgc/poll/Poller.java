package tgc.poll;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author aviolette
 * @since 11/5/12
 */
public class Poller implements Runnable {
  private final Twitter twitter;
  private final DBCollection config;
  private final DBCollection tweets;
  private final int twitterListId;
  private static final Logger log = Logger.getLogger(Poller.class.getName());

  public Poller(DBCollection config, DBCollection tweets, Twitter twitter, int twitterListId) {
    this.config = config;
    this.tweets = tweets;
    this.twitter = twitter;
    this.twitterListId = twitterListId;
  }

  @Override
  public void run() {
    Paging paging = determinePaging();
    boolean first = true;
    List<Status> statuses;
    try {
      statuses = twitter.getUserListStatuses(twitterListId, paging);
      for (Status status : statuses) {
        if (first) {
          // tweets come in descending order...so this is the tweet we want to start from next time
          config.update(new BasicDBObject("$exists", new BasicDBObject("_id", "true")),
              new BasicDBObject("lastId", status.getId()), true, false);
          first = false;
        }
        DBObject tweet = statusToDBObject(status);
        tweets.insert(tweet);
      }
    } catch (TwitterException e) {
      throw Throwables.propagate(e);
    }
  }

  private DBObject statusToDBObject(Status status) {
    final GeoLocation geoLocation = status.getGeoLocation();
    final String screenName = status.getUser().getScreenName().toLowerCase();
    final DateTime tweetTime = new DateTime(status.getCreatedAt(), DateTimeZone.UTC);
    DBObject obj = new BasicDBObject();
    obj.put("userId", screenName);
    obj.put("tweetId", status.getId());
    obj.put("retweet", status.isRetweet());
    if (geoLocation != null) {
      BasicDBObject location = new BasicDBObject();
      location.put("lat", geoLocation.getLatitude());
      location.put("lng", geoLocation.getLongitude());
      obj.put("location", location);
    }
    obj.put("time", tweetTime.getMillis());
    obj.put("text", status.getText());
    return obj;
  }

  private Paging determinePaging() {
    DBObject configObj = config.findOne();
    Paging paging;
    if (configObj == null) {
      paging = new Paging();
    } else {
      paging = new Paging((Long)configObj.get("lastId"));
    }
    return paging;
  }
}
