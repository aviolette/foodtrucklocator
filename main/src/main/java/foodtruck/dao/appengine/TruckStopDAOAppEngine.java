package foodtruck.dao.appengine;

import java.util.Collection;
import java.util.List;

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
import com.google.inject.Provider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.TruckStop;

import static com.google.appengine.api.datastore.Query.CompositeFilterOperator.and;
import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL;
import static com.google.appengine.api.datastore.Query.SortDirection.ASCENDING;
import static foodtruck.dao.appengine.Attributes.getDateTime;
import static foodtruck.dao.appengine.Attributes.getListProperty;
import static foodtruck.dao.appengine.Attributes.getStringProperty;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
class TruckStopDAOAppEngine extends AppEngineDAO<Long, TruckStop> implements TruckStopDAO {
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
  private static final String NOTES = "notes";
  private static final String ORIGIN = "origin";
  private static final String DEVICE_ID = "device_id";
  private static final String MANUALLY_UPDATED = "manually_updated";
  private static final Predicate<TruckStop> VENDOR_STOP_PREDICATE = new Predicate<TruckStop>() {
    public boolean apply(TruckStop truckStop) {
      return truckStop.getOrigin() == StopOrigin.VENDORCAL;
    }
  };

  private final DateTimeZone zone;
  private final TruckDAO truckDAO;

  @Inject
  public TruckStopDAOAppEngine(Provider<DatastoreService> provider, DateTimeZone zone, TruckDAO truckDAO) {
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
    return new FluidEntity(truckStop).prop(TRUCK_ID_FIELD, stop.getTruck()
        .getId())
        .prop(START_TIME_FIELD, stop.getStartTime()
            .toDate())
        .prop(END_TIME_FIELD, stop.getEndTime()
            .toDate())
        .prop(LATITUDE_FIELD, stop.getLocation()
            .getLatitude())
        .prop(LONGITUDE_FIELD, stop.getLocation()
            .getLongitude())
        .prop(LOCATION_NAME_FIELD, stop.getLocation()
            .getName())
        .prop(DESCRIPTION_FIELD, stop.getLocation()
            .getDescription())
        .prop(URL_FIELD, stop.getLocation()
            .getUrl())
        .prop(LOCKED_FIELD, stop.isLocked())
        .prop(ORIGIN, stop.getOrigin()
            .toString())
        .prop(NOTES, stop.getNotes())
        .prop(MANUALLY_UPDATED, stop.getManuallyUpdated())
        .prop(BEACON_FIELD, stop.getBeaconTime())
        .prop(LAST_UPDATED, stop.getLastUpdated())
        .prop(END_TIMESTAMP, stop.getEndTime()
            .getMillis())
        .prop(START_TIMESTAMP, stop.getStartTime()
            .getMillis())
        .prop(DEVICE_ID, stop.getCreatedWithDeviceId())
        .toEntity();
  }

  @Override
  protected TruckStop fromEntity(Entity entity) {
    Boolean locked = (Boolean) entity.getProperty(LOCKED_FIELD);
    Collection<String> notes = getListProperty(entity, NOTES);
    final String origin = getStringProperty(entity, ORIGIN);
    return TruckStop.builder()
        .truck(truckDAO.findById(getStringProperty(entity, TRUCK_ID_FIELD)))
        .startTime(getDateTime(entity, START_TIME_FIELD, zone))
        .notes(notes == null ? ImmutableList.<String>of() : ImmutableList.copyOf(notes))
        .endTime(getDateTime(entity, END_TIME_FIELD, zone))
        .origin(Strings.isNullOrEmpty(origin) ? StopOrigin.UNKNOWN : StopOrigin.valueOf(origin))
        .lastUpdated(Attributes.getDateTime(entity, LAST_UPDATED, zone))
        .manuallyUpdated(Attributes.getDateTime(entity, MANUALLY_UPDATED, zone))
        .fromBeacon(Attributes.getDateTime(entity, BEACON_FIELD, zone))
        .createdWithDeviceId((Long) entity.getProperty(DEVICE_ID))
        .location(Location.builder()
            .lat((Double) entity.getProperty(LATITUDE_FIELD))
            .lng((Double) entity.getProperty(LONGITUDE_FIELD))
            .name((String) entity.getProperty(LOCATION_NAME_FIELD))
            .description((String) entity.getProperty(DESCRIPTION_FIELD))
            .url((String) entity.getProperty(URL_FIELD))
            .build())
        .key(entity.getKey()
            .getId())
        .locked(locked == null ? false : locked)
        .build();
  }

  @Override
  public List<TruckStop> findDuring(@Nullable String truckId, LocalDate day) {
    final DateTime midnight = day.toDateTimeAtStartOfDay(zone);
    return aq().filter(and(trucksOverRange(truckId, new Interval(midnight.toDateTime()
        .minusHours(6), day.plusDays(1)
        .toDateTimeAtStartOfDay(zone)))))
        .sort(START_TIME_FIELD, ASCENDING)
        .execute(new Predicate<Entity>() {
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
    deleteFromQuery(dataStore,
        query().setFilter(predicate(START_TIME_FIELD, GREATER_THAN_OR_EQUAL, startTime.toDate())));
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
    Query q = new Query(STOP_KIND);
    List<Query.Filter> filters = trucksOverRange(truckId, range);
    q.setFilter(and(filters));
    q.addSort(START_TIME_FIELD, ASCENDING);
    return executeQuery(q, null);
  }

  @Override
  public
  @Nullable
  TruckStop findFirstStop(String truckId) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(STOP_KIND);
    q.addSort(START_TIME_FIELD, ASCENDING);
    q.setFilter(new Query.FilterPredicate(TRUCK_ID_FIELD, EQUAL, truckId));
    Iterable<Entity> items = dataStore.prepare(q)
        .asList(FetchOptions.Builder.withLimit(1));
    Entity item = Iterables.getFirst(items, null);
    if (item != null) {
      return fromEntity(item);
    }
    return null;
  }

  @Override
  public List<TruckStop> findAfter(String truckId, DateTime endTime) {
    Query q = query().setFilter(and(predicate(START_TIME_FIELD, GREATER_THAN_OR_EQUAL, endTime.toDate()),
        predicate(TRUCK_ID_FIELD, EQUAL, truckId)))
        .addSort(START_TIME_FIELD, ASCENDING);
    ImmutableList.Builder<TruckStop> stops = ImmutableList.builder();
    for (Entity entity : provider.get()
        .prepare(q)
        .asIterable(FetchOptions.Builder.withChunkSize(100))) {
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
    filters.add(new Query.FilterPredicate(START_TIME_FIELD, GREATER_THAN_OR_EQUAL, range.getStart()
        .toDate()));
    filters.add(new Query.FilterPredicate(START_TIME_FIELD, Query.FilterOperator.LESS_THAN, range.getEnd()
        .toDate()));
    if (truckId != null) {
      filters.add(new Query.FilterPredicate(TRUCK_ID_FIELD, EQUAL, truckId));
    }
    return filters;
  }
}
