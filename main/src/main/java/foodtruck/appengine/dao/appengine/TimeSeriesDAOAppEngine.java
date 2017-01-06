package foodtruck.appengine.dao.appengine;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Provider;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import foodtruck.dao.TimeSeriesDAO;
import foodtruck.model.Slots;
import foodtruck.model.SystemStats;

import static com.google.appengine.api.datastore.Query.CompositeFilterOperator.and;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN;
import static com.google.appengine.api.datastore.Query.SortDirection.ASCENDING;

/**
 * @author aviolette
 * @since 2/27/14
 */
abstract class TimeSeriesDAOAppEngine extends AppEngineDAO<Long, SystemStats> implements TimeSeriesDAO {
  private static final String PARAM_TIMESTAMP = "timestamp";
  private static final Logger log = Logger.getLogger(FifteenMinuteRollupDAOAppEngine.class.getName());
  private final Slots slotter;

  TimeSeriesDAOAppEngine(String kind, Provider<DatastoreService> provider, Slots slotter) {
    super(kind, provider);
    this.slotter = slotter;
  }

  public Slots getSlots() {
    return slotter;
  }

  @Override
  public List<SystemStats> findWithinRange(long startTime, long endTime, String[] statList) {
    return aq().filter(and(predicate(PARAM_TIMESTAMP, GREATER_THAN_OR_EQUAL, startTime),
        predicate(PARAM_TIMESTAMP, LESS_THAN, endTime)))
        .sort(PARAM_TIMESTAMP, ASCENDING)
        .execute();
  }

  @Override
  public void updateCount(DateTime timestamp, String key) {
    updateCount(timestamp, key, 1L);
  }

  @Override
  public void deleteBefore(LocalDate localDate) {
    DatastoreService dataStore = provider.get();
    long ts = localDate.toDateTimeAtStartOfDay()
        .getMillis();
    Query q = new Query(getKind()).setFilter(new Query.FilterPredicate(PARAM_TIMESTAMP, LESS_THAN, ts));
    List<Key> entities = Lists.newLinkedList();
    for (Entity e : dataStore.prepare(q)
        .asIterable()) {
      entities.add(e.getKey());
    }
    log.log(Level.INFO, "Deleted {0} entities", entities.size());
    dataStore.delete(entities);
  }

  @Override
  public void updateCount(DateTime timestamp, String statName, long by) {
    long slot = slotter.getSlot(timestamp.getMillis());
    updateCount(slot, statName, by);
  }

  @Override
  public void updateCount(long slot, String statName, long by) {
    DatastoreService dataStore = provider.get();
    Transaction txn = dataStore.beginTransaction();
    try {
      Entity entity = findBySlot(slot, dataStore);
      if (entity == null) {
        SystemStats stats = new SystemStats(-1, slot, ImmutableMap.of(statName, by));
        save(stats, dataStore);
      } else {
        long statValue = Attributes.getLongProperty(entity, statName, 0);
        entity.setProperty(statName, statValue + by);
        dataStore.put(entity);
      }
    } catch (DatastoreFailureException dsf) {
      // just ignore
      if (txn.isActive()) {
        txn.rollback();
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

  @Override
  public void deleteStat(String statName) {
    Query q = new Query(getKind());
    DatastoreService dataStore = provider.get();
    for (Entity e : dataStore.prepare(q)
        .asIterable()) {
      e.setProperty(statName, 0);
      dataStore.put(e);
    }
  }

  @Override
  public
  @Nullable
  SystemStats findBySlot(long slot) {
    Entity entity = findBySlot(slot, provider.get());
    if (entity == null) {
      return null;
    }
    return fromEntity(entity);
  }

  private
  @Nullable
  Entity findBySlot(long slot, DatastoreService dataStore) {
    Query q = new Query(getKind());
    q.setFilter(new Query.FilterPredicate(PARAM_TIMESTAMP, Query.FilterOperator.EQUAL, slot));
    return Iterables.getFirst(dataStore.prepare(q)
        .asIterable(), null);
  }

  @Override
  protected Entity toEntity(SystemStats obj, Entity entity) {
    entity.setProperty(PARAM_TIMESTAMP, obj.getTimeStamp());
    for (Map.Entry<String, Long> entry : obj.getAttributes()
        .entrySet()) {
      entity.setProperty(entry.getKey(), entry.getValue());
    }
    return entity;
  }

  @Override
  protected SystemStats fromEntity(Entity entity) {
    ImmutableMap.Builder<String, Long> entries = ImmutableMap.builder();
    for (Map.Entry<String, Object> entry : entity.getProperties()
        .entrySet()) {
      if (!entry.getKey()
          .equals(PARAM_TIMESTAMP)) {
        entries.put(entry.getKey(), (Long) entry.getValue());
      }
    }
    return new SystemStats(entity.getKey()
        .getId(), (Long) entity.getProperty(PARAM_TIMESTAMP), entries.build());
  }

}
