// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.dao.appengine;

import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.joda.time.DateTime;

import foodtruck.dao.SystemStatDAO;
import foodtruck.model.SystemStats;

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

  @Override
  public SystemStats findByTimestamp(DateTime timeStamp) {
    long ts = timeStamp.getMillis();
    long slot = (long) Math.floor((double) ts / (double) FIFTEEN_MIN_IN_MS) * FIFTEEN_MIN_IN_MS;
    SystemStats stats = findBySlot(slot);
    if (stats == null) {
      stats = new SystemStats(-1, slot, ImmutableMap.<String, Long>of());
      long id = save(stats);
      stats = findById(id);
    }
    return stats;
  }

  private SystemStats findBySlot(long slot) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(getKind());
    q.addFilter(PARAM_TIMESTAMP, Query.FilterOperator.EQUAL, slot);
    Entity entity = dataStore.prepare(q).asSingleEntity();
    if (entity == null) {
      return null;
    }
    return fromEntity(entity);
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
