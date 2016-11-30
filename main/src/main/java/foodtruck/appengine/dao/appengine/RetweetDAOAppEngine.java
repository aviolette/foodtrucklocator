package foodtruck.appengine.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.RetweetsDAO;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 8/5/13
 */
class RetweetDAOAppEngine implements RetweetsDAO {
  private static final String RETWEETS_KIND = "retweets";
  private static final String TWITTER_HANDLE = "twitter_handle";
  private static final String TRUCK_ID = "truck_id";
  private static final String TIMESTAMP = "timestamp";
  private final Clock clock;
  private final Provider<DatastoreService> provider;

  @Inject
  public RetweetDAOAppEngine(Provider<DatastoreService> provider, Clock clock) {
    this.provider = provider;
    this.clock = clock;
  }

  @Override
  public boolean hasBeenRetweeted(String truckId, String twitterHandle) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(RETWEETS_KIND);
    Query.Filter truckIdFilter = new Query.FilterPredicate(TRUCK_ID, Query.FilterOperator.EQUAL, truckId);
    Query.Filter twitterHandleFilter = new Query.FilterPredicate(TWITTER_HANDLE, Query.FilterOperator.EQUAL,
        twitterHandle);
    q.setFilter(Query.CompositeFilterOperator.and(truckIdFilter, twitterHandleFilter));
    return dataStore.prepare(q)
        .asQueryResultIterator()
        .hasNext();
  }

  @Override
  public void markRetweeted(String truckId, String twitterHandle) {
    DatastoreService dataStore = provider.get();
    Entity e = new Entity(RETWEETS_KIND, truckId + twitterHandle);
    e.setProperty(TRUCK_ID, truckId);
    e.setProperty(TWITTER_HANDLE, twitterHandle);
    e.setProperty(TIMESTAMP, clock.now()
        .toDate());
    dataStore.put(e);
  }

  @Override
  public void deleteAll() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(RETWEETS_KIND);
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q)
        .asIterable()) {
      keys.add(entity.getKey());
    }
    dataStore.delete(keys.build());
  }
}
