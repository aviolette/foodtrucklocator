package foodtruck.dao.appengine;

import java.util.List;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import foodtruck.dao.StoryDAO;
import foodtruck.model.Location;
import foodtruck.model.Story;
import foodtruck.model.StoryType;

import static foodtruck.dao.appengine.Attributes.getStringProperty;
import static foodtruck.dao.appengine.Attributes.getTextProperty;

/**
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
class StoryDAOAppEngine implements StoryDAO {
  private static final String TWEET_KIND = "stories";
  private static final String TWEET_SINCE_KIND = "TweetsSince";
  private static final String TWEET_SCREEN_NAME = "screen_name";
  private static final String TWEET_LOCATION_LAT = "lat";
  private static final String TWEET_LOCATION_LNG = "lng";
  private static final String TWEET_TEXT = "message";
  private static final String TWEET_TIME = "time";
  private static final String TWEET_ID = "message_id";
  private static final String TWEET_SINCE = "lastTweetId";
  private static final String TWEET_IGNORE = "ignore";
  private static final String STORY_TYPE = "story_type";
  private final Provider<DatastoreService> provider;
  private final DateTimeZone zone;

  @Inject
  public StoryDAOAppEngine(Provider<DatastoreService> provider, DateTimeZone zone) {
    this.provider = provider;
    this.zone = zone;
  }

  @Override
  public List<Story> findTweetsAfter(DateTime time, String twitterHandle, boolean includeIgnored) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TWEET_KIND);
    List<Query.Filter> filters = Lists.newLinkedList();
    filters.add(new Query.FilterPredicate(TWEET_TIME, Query.FilterOperator.GREATER_THAN_OR_EQUAL, time.toDate()));
    filters.add(new Query.FilterPredicate(TWEET_SCREEN_NAME, Query.FilterOperator.EQUAL, twitterHandle));
    if (!includeIgnored) {
      filters.add(new Query.FilterPredicate(TWEET_IGNORE, Query.FilterOperator.EQUAL, false));
    }
    q.setFilter(Query.CompositeFilterOperator.and(filters));
    q.addSort(TWEET_TIME, Query.SortDirection.DESCENDING);
    ImmutableList.Builder<Story> tweets = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q)
        .asIterable()) {
      Story tweet = fromEntity(entity);
      tweets.add(tweet);
    }
    return tweets.build();
  }

  @Override
  public void deleteBefore(DateTime dateTime) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TWEET_KIND);
    q.setFilter(new Query.FilterPredicate(TWEET_TIME, Query.FilterOperator.LESS_THAN_OR_EQUAL, dateTime.toDate()));
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q)
        .asIterable()) {
      keys.add(entity.getKey());
    }
    dataStore.delete(keys.build());
  }

  @Override
  public long getLastTweetId() {
    Query q = new Query(TWEET_SINCE_KIND);
    DatastoreService dataStore = provider.get();
    Entity entity = dataStore.prepare(q)
        .asSingleEntity();
    if (entity == null) {
      return 0;
    }
    return (Long) entity.getProperty(TWEET_SINCE);
  }

  @Override
  public void setLastTweetId(long id) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TWEET_SINCE_KIND);
    Entity e = dataStore.prepare(q)
        .asSingleEntity();
    if (e == null) {
      e = new Entity(TWEET_SINCE_KIND);
    }
    e.setProperty(TWEET_SINCE, id);
    dataStore.put(e);
  }

  @Override
  public void save(List<Story> tweets) {
    DatastoreService dataStore = provider.get();
    for (Story tweet : tweets) {
      saveOrUpdate(tweet, dataStore);
    }
  }

  @Override
  public
  @Nullable
  Story findByTweetId(long id) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TWEET_KIND);
    q.setFilter(new Query.FilterPredicate(TWEET_ID, Query.FilterOperator.EQUAL, id));
    Entity entity = dataStore.prepare(q)
        .asSingleEntity();
    if (entity == null) {
      return null;
    }
    return fromEntity(entity);
  }

  @Override
  public void saveOrUpdate(Story tweet) {
    DatastoreService dataStore = provider.get();
    saveOrUpdate(tweet, dataStore);
  }

  private void saveOrUpdate(Story tweet, DatastoreService dataStore) {
    final Entity entity = (tweet.getKey() == null) ? new Entity(TWEET_KIND) : new Entity((Key) tweet.getKey());
    entity.setProperty(TWEET_SCREEN_NAME, tweet.getScreenName());
    final Location location = tweet.getLocation();
    if (location != null) {
      entity.setProperty(TWEET_LOCATION_LAT, location.getLatitude());
      entity.setProperty(TWEET_LOCATION_LNG, location.getLongitude());
    }
    entity.setProperty(TWEET_TEXT, new Text(tweet.getText()));
    entity.setProperty(TWEET_TIME, tweet.getTime()
        .toDate());
    entity.setProperty(TWEET_ID, tweet.getId());
    entity.setProperty(TWEET_IGNORE, tweet.getIgnoreInTwittalyzer());
    entity.setProperty(STORY_TYPE, tweet.getStoryType()
        .toString());
    dataStore.put(entity);
  }

  private Story fromEntity(Entity entity) {
    Double lat = (Double) entity.getProperty(TWEET_LOCATION_LAT);
    Double lng = (Double) entity.getProperty(TWEET_LOCATION_LNG);
    Location location = null;
    if (lat != null && lng != null) {
      location = Location.builder()
          .lat(lat)
          .lng(lng)
          .build();
    }
    DateTime dateTime = new DateTime(entity.getProperty(TWEET_TIME), zone);
    StoryType storyType = StoryType.valueOf(getStringProperty(entity, STORY_TYPE, StoryType.TWEET.toString()));
    return new Story.Builder().id((Long) entity.getProperty(TWEET_ID))
        .ignoreInTwittalyzer(Boolean.TRUE.equals(entity.getProperty(TWEET_IGNORE)))
        .location(location)
        .time(dateTime)
        .key(entity.getKey())
        .text(getTextProperty(entity, TWEET_TEXT))
        .userId((String) entity.getProperty(TWEET_SCREEN_NAME))
        .type(storyType)
        .build();
  }
}
