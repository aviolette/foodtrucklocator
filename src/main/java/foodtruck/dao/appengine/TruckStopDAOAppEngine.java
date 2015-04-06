package foodtruck.dao.appengine;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
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

import static foodtruck.dao.appengine.Attributes.getDateTime;
import static foodtruck.dao.appengine.Attributes.getListProperty;
import static foodtruck.dao.appengine.Attributes.getStringProperty;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class TruckStopDAOAppEngine extends AppEngineDAO<Long, TruckStop> implements TruckStopDAO {
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
  public static final Predicate<TruckStop> VENDOR_STOP_PREDICATE = new Predicate<TruckStop>() {
    public boolean apply(TruckStop truckStop) {
      return truckStop.getOrigin() == StopOrigin.VENDORCAL;
    }
  };

  private final DateTimeZone zone;
  private final TruckDAO truckDAO;

  @Inject
  public TruckStopDAOAppEngine(DatastoreServiceProvider provider,
      DateTimeZone zone, TruckDAO truckDAO) {
    super(STOP_KIND, provider);
    this.zone = zone;
    this.truckDAO = truckDAO;
  }

  /**
   * Adds the specified stops to the data store.
   */
  @Override
  public void addStops(List<TruckStop> stops) {
    for (TruckStop stop : stops) {
      save(stop);
    }
  }

  @Override
  protected Entity toEntity(TruckStop stop, Entity truckStop) {
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
    return truckStop;
  }

  @Override
  protected TruckStop fromEntity(Entity entity) {
    Boolean locked = (Boolean) entity.getProperty(LOCKED_FIELD);
    final String confidence = getStringProperty(entity, MATCH_CONFIDENCE);
    Collection<String> notes = getListProperty(entity, NOTES);
    final String origin = getStringProperty(entity, ORIGIN);
    return TruckStop.builder()
        .truck(truckDAO.findById(getStringProperty(entity, TRUCK_ID_FIELD)))
        .startTime(getDateTime(entity, START_TIME_FIELD, zone))
        .notes(notes == null ? ImmutableList.<String>of() : ImmutableList.copyOf(notes))
        .endTime(getDateTime(entity, END_TIME_FIELD, zone))
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
  public List<TruckStop> findDuring(@Nullable String truckId, LocalDate day) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(STOP_KIND);
    final DateMidnight midnight = day.toDateMidnight(zone);
    // This opens up a 6 hour window before the specified day to get any stops that start on the prior day
    // This is not the best way to do this.  This code needs to be refactored
    List<Query.Filter> filters = trucksOverRange(truckId, new Interval(midnight.toDateTime().minusHours(6),
        day.plusDays(1).toDateTimeAtStartOfDay(zone)));
    q.setFilter(Query.CompositeFilterOperator.and(filters));
    q.addSort(START_TIME_FIELD, Query.SortDirection.ASCENDING);
    return executeQuery(dataStore, q, new Predicate<Entity>() {
      public boolean apply(Entity entity) {
        final DateTime startTime = new DateTime(entity.getProperty(START_TIME_FIELD), zone);
        final DateTime endTime = getDateTime(entity, END_TIME_FIELD, zone);
        // make sure that this stop at least ends or starts on the current day
        return midnight.isBefore(endTime) || midnight.isBefore(startTime);
      }
    });
  }

  @Override
  public void deleteAfter(DateTime startTime) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(STOP_KIND);
    q.setFilter(new Query.FilterPredicate(START_TIME_FIELD,
        Query.FilterOperator.GREATER_THAN_OR_EQUAL, startTime.toDate()));
    deleteFromQuery(dataStore, q);
  }

  @Override
  public void deleteStops(List<TruckStop> toDelete) {
    DatastoreService dataStore = provider.get();
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    for (TruckStop stop : toDelete) {
      keys.add(getKey(stop.getKey()));
    }
    dataStore.delete(keys.build());
  }

  @Override
  public List<TruckStop> findOverRange(@Nullable String truckId, Interval range) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(STOP_KIND);
    List<Query.Filter> filters = trucksOverRange(truckId, range);
    q.setFilter(Query.CompositeFilterOperator.and(filters));
    q.addSort(START_TIME_FIELD, Query.SortDirection.ASCENDING);
    return executeQuery(dataStore, q, null);
  }

  @Override
  public @Nullable TruckStop findFirstStop(String truckId) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(STOP_KIND);
    q.addSort(START_TIME_FIELD, Query.SortDirection.ASCENDING);
    q.setFilter(new Query.FilterPredicate(TRUCK_ID_FIELD, Query.FilterOperator.EQUAL, truckId));
    Iterable<Entity> items = dataStore.prepare(q).asList(FetchOptions.Builder.withLimit(1));
    Entity item = Iterables.getFirst(items, null);
    if (item != null) {
      return fromEntity(item);
    }
    return null;
  }

  @Override
  public List<TruckStop> findAfter(String truckId, DateTime endTime) {
    List<Query.Filter> filters = Lists.newLinkedList();
    filters.add(new Query.FilterPredicate(START_TIME_FIELD,
        Query.FilterOperator.GREATER_THAN_OR_EQUAL, endTime.toDate()));
    filters.add(new Query.FilterPredicate(TRUCK_ID_FIELD, Query.FilterOperator.EQUAL, truckId));
    DatastoreService dataStore = provider.get();
    Query q = new Query(STOP_KIND);
    q.setFilter(Query.CompositeFilterOperator.and(filters));
    q.addSort(START_TIME_FIELD, Query.SortDirection.ASCENDING);
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q).asIterable(FetchOptions.Builder.withChunkSize(100))) {
      stops.add(fromEntity(entity));
    }
    return stops.build();
  }

  @Override
  public List<TruckStop> findAfter(DateTime startTime) {
    return findOverRange(null, new Interval(startTime, startTime.plusYears(1)));
  }

  @Override
  public List<TruckStop> findVendorStopsAfter(DateTime start, String truckId) {
    return FluentIterable.from(findAfter(truckId, start))
        .filter(new Predicate<TruckStop>() {
          public boolean apply(TruckStop truckStop) {
            return truckStop.getOrigin() == StopOrigin.VENDORCAL;
          }
        })
        .toList();
  }

  @Override
  public List<TruckStop> findVendorStopsAfter(DateTime start) {
    return FluentIterable.from(findAfter(start))
        .filter(VENDOR_STOP_PREDICATE)
        .toList();
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
