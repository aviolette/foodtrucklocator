package foodtruck.dao.appengine;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import foodtruck.dao.TimeSeriesDAO;
import foodtruck.model.SystemStats;
import foodtruck.util.Slots;

/**
 * @author aviolette
 * @since 2/27/14
 */
public abstract class TimeSeriesDAOAppEngine extends AppEngineDAO<Long, SystemStats> implements TimeSeriesDAO {
  private static final String PARAM_TIMESTAMP = "timestamp";
  private static final Logger log = Logger.getLogger(FifteenMinuteRollupDAOAppEngine.class.getName());
  private final Slots slotter;

  public TimeSeriesDAOAppEngine(String kind, DatastoreServiceProvider provider, Slots slotter) {
    super(kind, provider);
    this.slotter = slotter;
  }

  @Override public List<SystemStats> findWithinRange(long startTime, long endTime) {
    DatastoreService dataStore = provider.get();
    return executeQuery(dataStore, new Query(getKind())
      .setFilter(Query.CompositeFilterOperator.and(
          new Query.FilterPredicate(PARAM_TIMESTAMP, Query.FilterOperator.GREATER_THAN_OR_EQUAL, startTime),
          new Query.FilterPredicate(PARAM_TIMESTAMP, Query.FilterOperator.LESS_THAN, endTime)))
      .addSort(PARAM_TIMESTAMP, Query.SortDirection.ASCENDING));
  }

  @Override public void updateCount(DateTime timestamp, String key) {
    updateCount(timestamp, key, 1L);
  }

  @Override public void deleteBefore(LocalDate localDate) {
    DatastoreService dataStore = provider.get();
    long ts = localDate.toDateMidnight().getMillis();
    Query q = new Query(getKind())
        .setFilter(new Query.FilterPredicate(PARAM_TIMESTAMP, Query.FilterOperator.LESS_THAN, ts));
    List<Key> entities = Lists.newLinkedList();
    for (Entity e : dataStore.prepare(q).asIterable()) {
      entities.add(e.getKey());
    }
    log.log(Level.INFO, "Deleted {0} entities", entities.size());
    dataStore.delete(entities);
  }

  @Override public void updateCount(DateTime timestamp, String statName, long by) {
    long slot = slotter.getSlot(timestamp.getMillis());
    updateCount(slot, statName, by);
  }

  @Override public void updateCount(long slot, String statName, long by) {
    DatastoreService dataStore = provider.get();
    Transaction txn = dataStore.beginTransaction();
    try {
      Entity entity = findBySlot(slot, dataStore);
      if (entity == null) {
        SystemStats stats = new SystemStats(-1, slot, ImmutableMap.<String, Long>of(statName, by));
        save(stats, dataStore);
      } else {
        long statValue = Attributes.getLongProperty(entity, statName, 0);
        entity.setProperty(statName, statValue + by);
        dataStore.put(entity);
      }
    } catch (Exception e) {
      log.log(Level.WARNING, "Error saving slot: " + slot, e);
      if (txn.isActive()) {
        txn.rollback();
      }
    } finally {
      try {
        if (txn.isActive()) {
          txn.commit();
        }
      } catch (Exception e) {
        log.log(Level.WARNING, "Error commiting stats", e);
      }
    }
  }

  private @Nullable Entity findBySlot(long slot, DatastoreService dataStore) {
    Query q = new Query(getKind());
    q.setFilter(new Query.FilterPredicate(PARAM_TIMESTAMP, Query.FilterOperator.EQUAL, slot));
    return Iterables.getFirst(dataStore.prepare(q).asIterable(), null);
  }

  @Override protected Entity toEntity(SystemStats obj, Entity entity) {
    entity.setProperty(PARAM_TIMESTAMP, obj.getTimeStamp());
    for (Map.Entry<String, Long> entry : obj.getAttributes().entrySet()) {
      entity.setProperty(entry.getKey(), entry.getValue());
    }
    return entity;
  }

  @Override protected SystemStats fromEntity(Entity entity) {
    ImmutableMap.Builder<String, Long> entries = ImmutableMap.builder();
    for (Map.Entry<String, Object> entry : entity.getProperties().entrySet()) {
      if (!entry.getKey().equals(PARAM_TIMESTAMP)) {
        entries.put(entry.getKey(), (Long) entry.getValue());
      }
    }
    return new SystemStats(entity.getKey().getId(), (Long) entity.getProperty(PARAM_TIMESTAMP),
        entries.build());
  }

}
