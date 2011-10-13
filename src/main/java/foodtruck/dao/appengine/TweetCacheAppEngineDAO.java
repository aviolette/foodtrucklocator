package foodtruck.dao.appengine;

import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.inject.Inject;

import org.joda.time.DateTime;

import foodtruck.dao.TweetCacheDAO;
import foodtruck.model.Location;
import foodtruck.model.TweetSummary;

/**
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
public class TweetCacheAppEngineDAO implements TweetCacheDAO {
  private static final String TWEET_KIND = "Tweet";
  private static final String TWEET_SINCE_KIND = "TweetsSince";
  private static final String TWEET_SCREEN_NAME = "screenName";
  private static final String TWEET_LOCATION_LAT = "lat";
  private static final String TWEET_LOCATION_LNG = "lng";
  private static final String TWEET_TEXT = "tweetText";
  private static final String TWEET_TIME = "time";
  private static final String TWEET_ID = "tweetId";
  private static final String TWEET_SINCE = "lastTweetId";
  private final DatastoreServiceProvider provider;

  @Inject
  public TweetCacheAppEngineDAO(DatastoreServiceProvider provider) {
    this.provider = provider;
  }


  @Override
  public List<TweetSummary> findTweetsAfter(DateTime time, String truckId) {
    return null;
  }

  @Override
  public void deleteBefore(DateTime dateTime) {

  }

  @Override public void setLastTweetId(long id) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TWEET_SINCE_KIND);
    Entity e = dataStore.prepare(q).asSingleEntity();
    if (e == null) {
      e = new Entity(TWEET_SINCE_KIND);
    }
    e.setProperty(TWEET_SINCE, id);
    dataStore.put(e);
  }

  @Override public long getLastTweetId() {
    Query q = new Query(TWEET_SINCE_KIND);
    DatastoreService dataStore = provider.get();
    Entity entity = dataStore.prepare(q).asSingleEntity();
    if (entity == null) {
      return 0;
    }
    return (Long)entity.getProperty(TWEET_SINCE);
  }

  @Override
  public void save(List<TweetSummary> tweets) {
    DatastoreService dataStore = provider.get();
    for (TweetSummary tweet : tweets) {
      final Entity entity = new Entity(TWEET_KIND);
      entity.setProperty(TWEET_SCREEN_NAME, tweet.getScreenName());
      final Location location = tweet.getLocation();
      if (location != null) {
        entity.setProperty(TWEET_LOCATION_LAT, location.getLatitude());
        entity.setProperty(TWEET_LOCATION_LNG, location.getLongitude());
      }
      entity.setProperty(TWEET_TEXT, tweet.getText());
      entity.setProperty(TWEET_TIME, tweet.getTime().toDate());
      entity.setProperty(TWEET_ID, tweet.getId());
      dataStore.put(entity);
    }
  }
}
