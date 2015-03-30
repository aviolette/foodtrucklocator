package foodtruck.dao.appengine;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PropertyContainer;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.TruckStop;
import foodtruck.schedule.Confidence;

import static com.google.common.base.Preconditions.checkNotNull;
import static foodtruck.dao.appengine.Attributes.getDateTime;
import static foodtruck.dao.appengine.Attributes.getDoubleProperty;
import static foodtruck.dao.appengine.Attributes.getListProperty;
import static foodtruck.dao.appengine.Attributes.getStringProperty;

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
  private static final String LOCATION_NAME_FIELD = "locationName";
  private static final String LOCKED_FIELD = "locked";
  private static final String DESCRIPTION_FIELD = "description";
  private static final String URL_FIELD = "url";
  private static final String END_TIMESTAMP = "endTimeStamp";
  private static final String START_TIMESTAMP = "startTimeStamp";
  private static final String BEACON_FIELD = "beaconTime";
  private static final String LAST_UPDATED = "last_updated";
  private static final String MATCH_CONFIDENCE = "match_confidence";
  private static final String NOTES = "notes";
  private static final String ORIGIN = "origin";

  private final DatastoreServiceProvider serviceProvider;
  private final DateTimeZone zone;
  private final TruckDAO truckDAO;

  @Inject
  public TruckStopDAOAppEngine(DatastoreServiceProvider provider,
      DateTimeZone zone, TruckDAO truckDAO) {
    this.serviceProvider = provider;
    this.zone = zone;
    this.truckDAO = checkNotNull(truckDAO);
  }

  /**
   * Finds all the trucks after the specified date, where the specified date time is between start and end
   * times of the truck stop.
   */
  @Override
  public Set<TruckStop> findAt(DateTime instant) {
    DatastoreService dataStore = serviceProvider.get();
    Query q = new Query(STOP_KIND);
    // AppEngine can't do range queries on multiple columns.  Here we set an upper bound on start time a day
    // after the current day.  So this should return all the stops with a start date within a particular day.
    // We then further filter below based on end date.
    Query.CompositeFilter rangeFilter =
        Query.CompositeFilterOperator.and(
            new Query.FilterPredicate(START_TIME_FIELD, Query.FilterOperator.GREATER_THAN_OR_EQUAL,
                instant.minusHours(24).toDate()),
            new Query.FilterPredicate(START_TIME_FIELD, Query.FilterOperator.LESS_THAN_OR_EQUAL,
                instant.plusDays(1).toDateMidnight().toDate()));
    q.setFilter(rangeFilter);
    ImmutableSet.Builder<TruckStop> stops = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      final DateTime startTime = getDateTime(entity, START_TIME_FIELD, zone),
          endTime = getDateTime(entity, END_TIME_FIELD, zone),
          upperBound = endTime.minusMinutes(1);
      if (instant.isBefore(startTime) || instant.isAfter(upperBound)) {
        continue;
      }
      final String truckId = getStringProperty(entity, TRUCK_ID_FIELD);
      final Boolean locked = (Boolean) entity.getProperty(LOCKED_FIELD);
      final String confidence = getStringProperty(entity, MATCH_CONFIDENCE);
      final String origin = getStringProperty(entity, ORIGIN);
      Collection<String> notes = getListProperty(entity, NOTES);
      try {
        stops.add(
            TruckStop.builder()
                .truck(truckDAO.findById(truckId))
                .startTime(startTime)
                .confidence(Strings.isNullOrEmpty(confidence) ? Confidence.HIGH : Confidence.valueOf(confidence))
                .origin(Strings.isNullOrEmpty(origin) ? StopOrigin.UNKNOWN : StopOrigin.valueOf(origin))
                .endTime(endTime)
                .notes(notes == null ? ImmutableList.<String>of() : ImmutableList.copyOf(notes))
                .lastUpdated(getDateTime(entity, LAST_UPDATED, zone))
                .location(
                    Location.builder()
                        .lat(getDoubleProperty(entity, LATITUDE_FIELD, 0d))
                        .lng(getDoubleProperty(entity, LONGITUDE_FIELD, 0d))
                        .name(getStringProperty(entity, LOCATION_NAME_FIELD))
                        .description(getStringProperty(entity, DESCRIPTION_FIELD))
                        .url(getStringProperty(entity, URL_FIELD))
                        .build())
                .key(entity.getKey().getId()).locked(locked == null ? false : locked).build());
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

  void putProperties(TruckStop stop, PropertyContainer truckStop) {
    truckStop.setProperty(TRUCK_ID_FIELD, stop.getTruck().getId());
    truckStop.setProperty(START_TIME_FIELD, stop.getStartTime().toDate());
    truckStop.setProperty(END_TIME_FIELD, stop.getEndTime().toDate());
    truckStop.setProperty(LATITUDE_FIELD, stop.getLocation().getLatitude());
    truckStop.setProperty(LONGITUDE_FIELD, stop.getLocation().getLongitude());
    truckStop.setProperty(LOCATION_NAME_FIELD, stop.getLocation().getName());
    truckStop.setProperty(DESCRIPTION_FIELD, stop.getLocation().getDescription());
    truckStop.setProperty(URL_FIELD, stop.getLocation().getUrl());
    truckStop.setProperty(LOCKED_FIELD, stop.isLocked());
    truckStop.setProperty(MATCH_CONFIDENCE, stop.getConfidence().toString());
    truckStop.setProperty(ORIGIN, stop.getOrigin().toString());
    truckStop.setProperty(NOTES, stop.getNotes());
    Attributes.setDateProperty(BEACON_FIELD, truckStop, stop.getBeaconTime());
    Attributes.setDateProperty(LAST_UPDATED, truckStop, stop.getLastUpdated());
    truckStop.setProperty(END_TIMESTAMP, stop.getEndTime().getMillis());
    truckStop.setProperty(START_TIMESTAMP, stop.getStartTime().getMillis());
  }

  private Entity toEntity(TruckStop stop, Entity entity) {
    Entity truckStop = entity == null ? new Entity(STOP_KIND) : entity;
    putProperties(stop, truckStop);
    return truckStop;
  }

  @Override
  public List<TruckStop> findDuring(@Nullable String truckId, LocalDate day) {
    DatastoreService dataStore = serviceProvider.get();
    Query q = new Query(STOP_KIND);
    final DateMidnight midnight = day.toDateMidnight(zone);
    // This opens up a 6 hour window before the specified day to get any stops that start on the prior day
    // This is not the best way to do this.  This code needs to be refactored
    List<Query.Filter> filters = trucksOverRange(truckId, new Interval(midnight.toDateTime().minusHours(6),
        day.plusDays(1).toDateTimeAtStartOfDay(zone)));
    q.setFilter(Query.CompositeFilterOperator.and(filters));
    q.addSort(START_TIME_FIELD, Query.SortDirection.ASCENDING);
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      final DateTime startTime = new DateTime(entity.getProperty(START_TIME_FIELD), zone);
      final DateTime endTime = getDateTime(entity, END_TIME_FIELD, zone);
      // make sure that this stop at least ends or starts on the current day
      if (midnight.isBefore(endTime) || midnight.isBefore(startTime)) {
        stops.add(toTruckStop(entity));
      }
    }
    return stops.build();
  }

  private TruckStop toTruckStop(Entity entity) {
    final DateTime startTime = new DateTime(entity.getProperty(START_TIME_FIELD), zone);
    final DateTime endTime = new DateTime(entity.getProperty(END_TIME_FIELD), zone);
    Boolean locked = (Boolean) entity.getProperty(LOCKED_FIELD);
    final String confidence = getStringProperty(entity, MATCH_CONFIDENCE);
    Collection<String> notes = getListProperty(entity, NOTES);
    final String origin = getStringProperty(entity, ORIGIN);
    return TruckStop.builder()
        .truck(truckDAO.findById((String) entity.getProperty(TRUCK_ID_FIELD)))
        .startTime(startTime)
        .notes(notes == null ? ImmutableList.<String>of() : ImmutableList.copyOf(notes))
        .endTime(endTime)
        .origin(Strings.isNullOrEmpty(origin) ? StopOrigin.UNKNOWN : StopOrigin.valueOf(origin))
        .confidence(Strings.isNullOrEmpty(confidence) ? Confidence.HIGH : Confidence.valueOf(confidence))
        .lastUpdated(Attributes.getDateTime(entity, LAST_UPDATED, zone))
        .fromBeacon(Attributes.getDateTime(entity, BEACON_FIELD, zone))
        .location(Location.builder().lat((Double) entity.getProperty(LATITUDE_FIELD))
            .lng((Double) entity.getProperty(
                LONGITUDE_FIELD)).name((String) entity.getProperty(LOCATION_NAME_FIELD))
            .description((String) entity.getProperty(DESCRIPTION_FIELD))
            .url((String) entity.getProperty(URL_FIELD))
            .build())
        .key(entity.getKey().getId())
        .locked(locked == null ? false : locked)
        .build();
  }

  @Override
  public void deleteAfter(DateTime startTime) {
    DatastoreService dataStore = serviceProvider.get();
    Query q = new Query(STOP_KIND);
    q.setFilter(new Query.FilterPredicate(START_TIME_FIELD,
        Query.FilterOperator.GREATER_THAN_OR_EQUAL, startTime.toDate()));
    deleteFromQuery(dataStore, q);
  }

  protected void deleteFromQuery(DatastoreService dataStore, Query q) {
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
    List filters = Lists.newLinkedList();
    filters.add(new Query.FilterPredicate(START_TIME_FIELD,
        Query.FilterOperator.GREATER_THAN_OR_EQUAL, startTime.toDate()));
    filters.add(new Query.FilterPredicate(TRUCK_ID_FIELD, Query.FilterOperator.EQUAL, truckId));
    q.setFilter(Query.CompositeFilterOperator.and(filters));
    deleteFromQuery(dataStore, q);
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

  @Override public void save(TruckStop truckStop) {
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

  @Override
  public List<TruckStop> findOverRange(@Nullable String truckId, Interval range) {
    DatastoreService dataStore = serviceProvider.get();
    Query q = new Query(STOP_KIND);
    List<Query.Filter> filters = trucksOverRange(truckId, range);
    q.setFilter(Query.CompositeFilterOperator.and(filters));

    q.addSort(START_TIME_FIELD, Query.SortDirection.ASCENDING);
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q).asIterable(FetchOptions.Builder.withChunkSize(100))) {
      stops.add(toTruckStop(entity));
    }
    return stops.build();
  }

  @Override public TruckStop findFirstStop(String truckId) {
    DatastoreService dataStore = serviceProvider.get();
    Query q = new Query(STOP_KIND);
    q.addSort(START_TIME_FIELD, Query.SortDirection.ASCENDING);
    q.setFilter(new Query.FilterPredicate(TRUCK_ID_FIELD, Query.FilterOperator.EQUAL, truckId));
    Iterable<Entity> items = dataStore.prepare(q).asList(FetchOptions.Builder.withLimit(1));
    Entity item = Iterables.getFirst(items, null);
    if (item != null) {
      return toTruckStop(item);
    }
    return null;
  }

  @Override
  public List<TruckStop> findAfter(String truckId, DateTime endTime) {
    List<Query.Filter> filters = Lists.newLinkedList();
    filters.add(new Query.FilterPredicate(START_TIME_FIELD,
        Query.FilterOperator.GREATER_THAN_OR_EQUAL, endTime.toDate()));
    filters.add(new Query.FilterPredicate(TRUCK_ID_FIELD, Query.FilterOperator.EQUAL, truckId));
    DatastoreService dataStore = serviceProvider.get();
    Query q = new Query(STOP_KIND);
    q.setFilter(Query.CompositeFilterOperator.and(filters));
    q.addSort(START_TIME_FIELD, Query.SortDirection.ASCENDING);
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q).asIterable(FetchOptions.Builder.withChunkSize(100))) {
      stops.add(toTruckStop(entity));
    }
    return stops.build();
  }

  private List<Query.Filter> trucksOverRange(@Nullable String truckId, Interval range) {
    List<Query.Filter> filters = Lists.newLinkedList();
    filters.add(new Query.FilterPredicate(START_TIME_FIELD,
        Query.FilterOperator.GREATER_THAN_OR_EQUAL, range.getStart().toDate()));
    filters.add(new Query.FilterPredicate(START_TIME_FIELD,
        Query.FilterOperator.LESS_THAN, range.getEnd().toDate()));
    if (truckId != null) {
      filters.add(new Query.FilterPredicate(TRUCK_ID_FIELD, Query.FilterOperator.EQUAL, truckId));
    }
    return filters;
  }
}
