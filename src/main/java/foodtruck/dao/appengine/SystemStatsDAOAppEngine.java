package foodtruck.dao.appengine;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.joda.time.DateTime;

import foodtruck.dao.SystemStatDAO;
import foodtruck.model.SystemStats;
import foodtruck.stats.Slots;

/**
 * @author aviolette@gmail.com
 * @since 7/5/12
 */
public class SystemStatsDAOAppEngine extends AppEngineDAO<Long, SystemStats>
    implements SystemStatDAO {
  private final static long FIFTEEN_MIN_IN_MS = 900000;
  private static final String PARAM_TIMESTAMP = "timestamp";

  @Inject
  public SystemStatsDAOAppEngine(DatastoreServiceProvider provider) {
    super("fifteen_min_stat", provider);
  }

  @Override public List<SystemStats> findWithinRange(long startTime, long endTime) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(getKind());
    q.addFilter(PARAM_TIMESTAMP, Query.FilterOperator.GREATER_THAN_OR_EQUAL, startTime);
    q.addFilter(PARAM_TIMESTAMP, Query.FilterOperator.LESS_THAN, endTime);
    q.addSort(PARAM_TIMESTAMP, Query.SortDirection.ASCENDING);
    ImmutableList.Builder<SystemStats> stats = ImmutableList.builder();
    for (Entity e : dataStore.prepare(q).asIterable()) {
      stats.add(fromEntity(e));
    }
    return stats.build();
  }

  @Override public void updateCount(DateTime timestamp, String key) {
    updateCount(timestamp, key, 1L);
  }

  public void updateCount(DateTime timestamp, String statName, long by) {
    DatastoreService dataStore = provider.get();
    Transaction txn = dataStore.beginTransaction();
    try {
      long ts = Slots.getSlot(timestamp.getMillis());
      long slot = (long) Math.floor((double) ts / (double) FIFTEEN_MIN_IN_MS) * FIFTEEN_MIN_IN_MS;
      Entity entity = findBySlot(slot, dataStore);
      if (entity == null) {
        SystemStats stats = new SystemStats(-1, slot, ImmutableMap.<String, Long>of(statName, by));
        save(stats, dataStore);
      } else {
        long statValue = Attributes.getLongProperty(entity, statName, 0);
        entity.setProperty(statName, statValue + by);
        dataStore.put(entity);
      }
    } finally {
      if (txn.isActive()) {
        txn.commit();
      }
    }
  }

  private @Nullable Entity findBySlot(long slot, DatastoreService dataStore) {
    Query q = new Query(getKind());
    q.addFilter(PARAM_TIMESTAMP, Query.FilterOperator.EQUAL, slot);
    return dataStore.prepare(q).asSingleEntity();
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
