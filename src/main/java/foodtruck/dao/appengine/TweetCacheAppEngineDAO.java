package foodtruck.dao.appengine;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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
  private final DateTimeZone zone;

  @Inject
  public TweetCacheAppEngineDAO(DatastoreServiceProvider provider, DateTimeZone zone) {
    this.provider = provider;
    this.zone = zone;
  }


  @Override
  public List<TweetSummary> findTweetsAfter(DateTime time, String truckId) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TWEET_KIND);
    q.addFilter(TWEET_TIME, Query.FilterOperator.GREATER_THAN_OR_EQUAL,
        time.toDate());
    q.addFilter(TWEET_SCREEN_NAME, Query.FilterOperator.EQUAL, truckId);
    q.addSort(TWEET_TIME, Query.SortDirection.DESCENDING);
    ImmutableList.Builder<TweetSummary> tweets = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      Double lat = (Double) entity.getProperty(TWEET_LOCATION_LAT);
      Double lng = (Double) entity.getProperty(TWEET_LOCATION_LNG);
      Location location = null;
      if (lat != null && lng != null) {
        location = new Location(lat, lng);
      }
      DateTime dateTime = new DateTime((Date) entity.getProperty(TWEET_TIME), zone);
      TweetSummary tweet = new TweetSummary.Builder()
          .id((Long) entity.getProperty(TWEET_ID))
          .location(location)
          .time(dateTime)
          .text((String) entity.getProperty(TWEET_TEXT))
          .userId((String) entity.getProperty(TWEET_SCREEN_NAME))
          .build();
      tweets.add(tweet);
    }
    return tweets.build();
  }

  @Override
  public void deleteBefore(DateTime dateTime) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TWEET_KIND);
    q.addFilter(TWEET_TIME, Query.FilterOperator.LESS_THAN_OR_EQUAL, dateTime.toDate());
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      keys.add(entity.getKey());
    }
    dataStore.delete(keys.build());
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
    return (Long) entity.getProperty(TWEET_SINCE);
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
