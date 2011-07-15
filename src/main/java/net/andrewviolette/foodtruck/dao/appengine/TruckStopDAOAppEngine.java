package net.andrewviolette.foodtruck.dao.appengine;

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

import net.andrewviolette.foodtruck.dao.TruckStopDAO;
import net.andrewviolette.foodtruck.model.Location;
import net.andrewviolette.foodtruck.model.Truck;
import net.andrewviolette.foodtruck.model.TruckStop;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class TruckStopDAOAppEngine implements TruckStopDAO {
  private static final String STOP_KIND = "Truck";
  private static final String TIME_RANGE_FIELD = "timeRange";
  private static final String LATITUDE_FIELD = "latitude";
  private static final String LONGITUDE_FIELD = "longitude";
  private static final String TRUCK_ID_FIELD = "truckId";
  private final Map<String, Truck> trucks;

  @Inject
  public TruckStopDAOAppEngine(Map<String, Truck> trucks) {
    this.trucks = trucks;
  }

  /**
   * Finds all the trucks after the specified date.
   */
  @Override
  public Set<TruckStop> findAfter(DateTime instant) {
    DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
    Query q = new Query(STOP_KIND);
    q.addFilter(TIME_RANGE_FIELD, Query.FilterOperator.GREATER_THAN_OR_EQUAL, instant.toDate());
    ImmutableSet.Builder<TruckStop> stops = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      stops.add(new TruckStop(trucks.get((String)entity.getProperty(TRUCK_ID_FIELD)),
          new DateTime(((List)entity.getProperty(TIME_RANGE_FIELD)).get(0)),
          new DateTime(((List)entity.getProperty(TIME_RANGE_FIELD)).get(1)),
          new Location((Double)entity.getProperty(LATITUDE_FIELD), (Double)entity.getProperty(
              LONGITUDE_FIELD))));
    }
    return stops.build();
  }

  /**
   * Deletes all the stops after the specified date/time. 
   */
  @Override
  public void deleteAfter(DateTime startTime) {
    DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
    Query q = new Query(STOP_KIND);
    q.addFilter(TIME_RANGE_FIELD, Query.FilterOperator.GREATER_THAN_OR_EQUAL, startTime.toDate());
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
      truckStop.setProperty(TIME_RANGE_FIELD, ImmutableList.of(stop.getStartTime().toDate(), stop.getEndTime().toDate()));
      truckStop.setProperty(LATITUDE_FIELD, stop.getLocation().getLatitude());
      truckStop.setProperty(LONGITUDE_FIELD, stop.getLocation().getLongitude());
      service.put(truckStop);
    }
  }
}
