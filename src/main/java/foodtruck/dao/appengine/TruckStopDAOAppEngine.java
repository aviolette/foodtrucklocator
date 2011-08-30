package foodtruck.dao.appengine;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class TruckStopDAOAppEngine implements TruckStopDAO {
  private static final String STOP_KIND = "Truck";
  private static final String START_TIME_FIELD = "startTime";
  private static final String END_TIME_FIELD = "endTime";
  private static final String LATITUDE_FIELD = "latitude";
  private static final String LONGITUDE_FIELD = "longitude";
  private static final String TRUCK_ID_FIELD = "truckId";
  private final Map<String, Truck> trucks;
  private final DatastoreServiceProvider serviceProvider;
  private final DateTimeZone zone;

  @Inject
  public TruckStopDAOAppEngine(Map<String, Truck> trucks, DatastoreServiceProvider provider,
      DateTimeZone zone) {
    this.trucks = trucks;
    this.serviceProvider = provider;
    this.zone = zone;
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
      if (instant.isBefore(startTime) || instant.isAfter(endTime)) {
        continue;
      }
      stops.add(new TruckStop(trucks.get((String) entity.getProperty(TRUCK_ID_FIELD)),
          startTime, endTime,
          new Location((Double) entity.getProperty(LATITUDE_FIELD), (Double) entity.getProperty(
              LONGITUDE_FIELD))));
    }
    return stops.build();
  }

  /**
   * Deletes all the stops after the specified date/time.
   */
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

  /**
   * Adds the specified stops to the data store.
   */
  @Override
  public void addStops(List<TruckStop> stops) {
    DatastoreService service = DatastoreServiceFactory.getDatastoreService();
    for (TruckStop stop : stops) {
      Entity truckStop = new Entity(STOP_KIND);
      truckStop.setProperty(TRUCK_ID_FIELD, stop.getTruck().getId());
      truckStop.setProperty(START_TIME_FIELD, stop.getStartTime().toDate());
      truckStop.setProperty(END_TIME_FIELD, stop.getEndTime().toDate());
      truckStop.setProperty(LATITUDE_FIELD, stop.getLocation().getLatitude());
      truckStop.setProperty(LONGITUDE_FIELD, stop.getLocation().getLongitude());
      service.put(truckStop);
    }
  }
}
