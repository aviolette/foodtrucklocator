package foodtruck.truckstops;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.TimeRange;
import foodtruck.model.Truck;
import foodtruck.model.TruckLocationGroup;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStop;
import foodtruck.schedule.GoogleCalendarStrategy;
import foodtruck.schedule.ScheduleStrategy;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class FoodTruckStopServiceImpl implements FoodTruckStopService {
  private final TruckStopDAO truckStopDAO;
  private final ScheduleStrategy defaultStrategy;
  private final Map<String, Truck> trucks;
  private static final Logger log = Logger.getLogger(FoodTruckStopServiceImpl.class.getName());
  private final DateTimeZone zone;

  @Inject
  public FoodTruckStopServiceImpl(TruckStopDAO truckStopDAO, GoogleCalendarStrategy defaultStrategy,
      Map<String, Truck> trucks, DateTimeZone zone) {
    this.truckStopDAO = truckStopDAO;
    this.defaultStrategy = defaultStrategy;
    this.trucks = trucks;
    this.zone = zone;
  }

  @Override
  public void updateStopsFor(LocalDate instant) {
    TimeRange theDay = new TimeRange(instant, zone);
    truckStopDAO.deleteAfter(theDay.getStartDateTime());
    for (Truck truck : trucks.values()) {
      try {
        List<TruckStop> stops = defaultStrategy.findForTime(truck, theDay);
        truckStopDAO.addStops(stops);
      } catch (Exception e) {
        log.log(Level.WARNING, "Exception thrown while refreshing truck: " + truck.getId(), e);
      }
    }
  }

  @Override
  public Set<TruckLocationGroup> findFoodTruckGroups(DateTime dateTime) {
    Multimap<Location, Truck> locations = LinkedListMultimap.create();
    Set<Truck> allTrucks = com.google.appengine.repackaged.com.google.common.collect.Sets
        .newHashSet();
    allTrucks.addAll(trucks.values());
    for (TruckStop stop : truckStopDAO.findAt(dateTime)) {
      locations.put(stop.getLocation(), stop.getTruck());
      allTrucks.remove(stop.getTruck());
    }
    for (Truck truck : allTrucks) {
      locations.put(null, truck);
    }
    ImmutableSet.Builder<TruckLocationGroup> builder = ImmutableSet.builder();
    for (Location location : locations.keySet()) {
      builder.add(new TruckLocationGroup(location, locations.get(location)));
    }
    Collection c = locations.get(null);
    if (c != null && !c.isEmpty()) {
      builder.add(new TruckLocationGroup(null, c));
    }
    return builder.build();

  }

  @Override
  public TruckSchedule findStopsForDay(String truckId, LocalDate day) {
    Truck truck = trucks.get(truckId);
    if (truck == null) {
      throw new IllegalStateException("Invalid truck id specified: " + truckId);
    }
    return new TruckSchedule(truck, day, truckStopDAO.findDuring(truckId, day));
  }
}
