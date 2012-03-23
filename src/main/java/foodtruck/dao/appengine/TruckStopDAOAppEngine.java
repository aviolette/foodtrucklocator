package foodtruck.dao.appengine;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import static com.google.common.base.Preconditions.checkNotNull;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class TruckStopDAOAppEngine implements TruckStopDAO {
  private static final Logger log = Logger.getLogger(TruckStopDAOAppEngine.class.getName());
  private static final String STOP_KIND = "Truck";
  private static final String START_TIME_FIELD = "startTime";
  private static final String END_TIME_FIELD = "endTime";
  private static final String LATITUDE_FIELD = "latitude";
  private static final String LONGITUDE_FIELD = "longitude";
  private static final String TRUCK_ID_FIELD = "truckId";
  private final DatastoreServiceProvider serviceProvider;
  private final DateTimeZone zone;
  private static final String LOCATION_NAME_FIELD = "locationName";
  private final TruckDAO truckDAO;

  @Inject
  public TruckStopDAOAppEngine(DatastoreServiceProvider provider,
      DateTimeZone zone, TruckDAO truckDAO) {
    this.serviceProvider = provider;
    this.zone = zone;
    this.truckDAO = checkNotNull(truckDAO);
  }

  /**
   * Finds all the trucks after the specified date.
   */
  @Override
  public Set<TruckStop> findAt(DateTime instant) {
    DatastoreService dataStore = serviceProvider.get();
    Query q = new Query(STOP_KIND);
    // TODO: google's filters can't do range comparisons...ugh...here I search the lower bound
    q.addFilter(START_TIME_FIELD, Query.FilterOperator.GREATER_THAN_OR_EQUAL,
        instant.toDateMidnight().toDate());
    ImmutableSet.Builder<TruckStop> stops = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      final DateTime startTime = new DateTime((Date) entity.getProperty(START_TIME_FIELD), zone);
      final DateTime endTime = new DateTime((Date) entity.getProperty(END_TIME_FIELD), zone);
      final DateTime upperBound = endTime.minusMinutes(1);
      if (instant.isBefore(startTime) || instant.isAfter(upperBound)) {
        continue;
      }
      final String truckId = (String) entity.getProperty(TRUCK_ID_FIELD);
      try {
        stops.add(new TruckStop(truckDAO.findById(truckId),
            startTime, endTime,
            Location.builder().lat((Double) entity.getProperty(LATITUDE_FIELD))
                .lng((Double) entity.getProperty(
                    LONGITUDE_FIELD)).name((String) entity.getProperty(LOCATION_NAME_FIELD))
                .build(),
            entity.getKey().getId()));
      } catch (RuntimeException rt) {
        log.log(Level.WARNING, "Error for truckId: " + truckId, rt);
      }
    }
    return stops.build();
  }

  /**
   * Adds the specified stops to the data store.
   */
  @Override
  public void addStops(List<TruckStop> stops) {
    DatastoreService service = DatastoreServiceFactory.getDatastoreService();
    for (TruckStop stop : stops) {
      Entity truckStop = toEntity(stop, null);
      service.put(truckStop);
    }
  }

  private Entity toEntity(TruckStop stop, Entity entity) {
    Entity truckStop = entity == null ? new Entity(STOP_KIND) : entity;
    truckStop.setProperty(TRUCK_ID_FIELD, stop.getTruck().getId());
    truckStop.setProperty(START_TIME_FIELD, stop.getStartTime().toDate());
    truckStop.setProperty(END_TIME_FIELD, stop.getEndTime().toDate());
    truckStop.setProperty(LATITUDE_FIELD, stop.getLocation().getLatitude());
    truckStop.setProperty(LONGITUDE_FIELD, stop.getLocation().getLongitude());
    truckStop.setProperty(LOCATION_NAME_FIELD, stop.getLocation().getName());
    return truckStop;
  }

  @Override
  public List<TruckStop> findDuring(@Nullable String truckId, LocalDate day) {
    DatastoreService dataStore = serviceProvider.get();
    Query q = new Query(STOP_KIND);
    // TODO: google's filters can't do range comparisons...ugh...here I search the lower bound
    q.addFilter(START_TIME_FIELD, Query.FilterOperator.GREATER_THAN_OR_EQUAL,
        day.toDateMidnight(zone).toDate());
    if (truckId != null) {
      q.addFilter(TRUCK_ID_FIELD, Query.FilterOperator.EQUAL, truckId);
    }
    q.addSort(START_TIME_FIELD, Query.SortDirection.ASCENDING);
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      final DateTime startTime = new DateTime((Date) entity.getProperty(START_TIME_FIELD), zone);
      if (startTime.isAfter(day.plusDays(1).toDateMidnight(zone))) {
        continue;
      }
      stops.add(toTruckStop(entity));
    }
    return stops.build();
  }

  private TruckStop toTruckStop(Entity entity) {
    final DateTime startTime = new DateTime((Date) entity.getProperty(START_TIME_FIELD), zone);
    final DateTime endTime = new DateTime((Date) entity.getProperty(END_TIME_FIELD), zone);
    return new TruckStop(truckDAO.findById((String) entity.getProperty(TRUCK_ID_FIELD)),
        startTime, endTime,
        Location.builder().lat((Double) entity.getProperty(LATITUDE_FIELD))
            .lng((Double) entity.getProperty(
                LONGITUDE_FIELD)).name((String) entity.getProperty(LOCATION_NAME_FIELD)).build(),
        entity.getKey().getId());
  }

  @Override
  public void deleteAfter(DateTime startTime) {
    DatastoreService dataStore = serviceProvider.get();
    Query q = new Query(STOP_KIND);
    q.addFilter(START_TIME_FIELD, Query.FilterOperator.GREATER_THAN_OR_EQUAL, startTime.toDate());
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      keys.add(entity.getKey());
    }
    dataStore.delete(keys.build());
  }

  @Override
  public void deleteAfter(DateTime startTime, String truckId) {
    DatastoreService dataStore = serviceProvider.get();
    Query q = new Query(STOP_KIND);
    q.addFilter(START_TIME_FIELD, Query.FilterOperator.GREATER_THAN_OR_EQUAL, startTime.toDate());
    q.addFilter(TRUCK_ID_FIELD, Query.FilterOperator.EQUAL, truckId);
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      keys.add(entity.getKey());
    }
    dataStore.delete(keys.build());
  }

  @Override
  public void deleteStops(List<TruckStop> toDelete) {
    DatastoreService dataStore = serviceProvider.get();
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    for (TruckStop stop : toDelete) {
      keys.add(findKey(stop));
    }
    dataStore.delete(keys.build());
  }

  public Key findKey(TruckStop stop) {
    return KeyFactory.createKey(STOP_KIND, (Long) stop.getKey());
  }

  @Override public TruckStop findById(long stopId) {
    DatastoreService dataStore = serviceProvider.get();
    Key key = KeyFactory.createKey(STOP_KIND, stopId);
    try {
      Entity entity = dataStore.get(key);
      return toTruckStop(entity);
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  @Override public void delete(long stopId) {
    DatastoreService dataStore = serviceProvider.get();
    Key key = KeyFactory.createKey(STOP_KIND, stopId);
    dataStore.delete(key);
  }

  @Override public void update(TruckStop truckStop) {
    DatastoreService dataStore = serviceProvider.get();
    Entity entity = null;
    try {
      if (!truckStop.isNew()) {
        Key key = findKey(truckStop);
        entity = dataStore.get(key);
      }
      entity = toEntity(truckStop, entity);
      dataStore.put(entity);
    } catch (EntityNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
