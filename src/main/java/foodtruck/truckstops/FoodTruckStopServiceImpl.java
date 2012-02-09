package foodtruck.truckstops;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import foodtruck.dao.TruckStopDAO;
import foodtruck.model.TruckStatus;
import foodtruck.model.DailySchedule;
import foodtruck.model.Location;
import foodtruck.model.TimeRange;
import foodtruck.model.Truck;
import foodtruck.model.TruckLocationGroup;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStop;
import foodtruck.model.Trucks;
import foodtruck.schedule.GoogleCalendar;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class FoodTruckStopServiceImpl implements FoodTruckStopService {
  private final TruckStopDAO truckStopDAO;
  private final GoogleCalendar googleCalendar;
  private final Trucks trucks;
  private static final Logger log = Logger.getLogger(FoodTruckStopServiceImpl.class.getName());
  private final DateTimeZone zone;
  private final Clock clock;

  @Inject
  public FoodTruckStopServiceImpl(TruckStopDAO truckStopDAO, GoogleCalendar googleCalendar,
      Trucks trucks, DateTimeZone zone, Clock clock) {
    this.truckStopDAO = truckStopDAO;
    this.googleCalendar = googleCalendar;
    this.trucks = trucks;
    this.zone = zone;
    this.clock = clock;
  }

  @Override
  public void updateStopsForTruck(LocalDate instant, Truck truck) {
    TimeRange theDay = new TimeRange(instant, zone);
    List<TruckStop> stops = googleCalendar.findForTime(theDay, truck);
    truckStopDAO.deleteAfter(theDay.getStartDateTime(), truck.getId());
    truckStopDAO.addStops(stops);
  }

  @Override public @Nullable TruckStop findById(long stopId) {
    return truckStopDAO.findById(stopId);
  }

  @Override public void delete(long stopId) {
    truckStopDAO.delete(stopId);
  }

  @Override public void update(TruckStop truckStop) {
    truckStopDAO.update(truckStop);
  }

  @Override
  public void updateStopsFor(LocalDate instant) {
    TimeRange theDay = new TimeRange(instant, zone);
    pullTruckSchedule(theDay);
  }

  private void pullTruckSchedule(TimeRange theDay) {
    try {
      List<TruckStop> stops = googleCalendar.findForTime(theDay, null);
      truckStopDAO.deleteAfter(theDay.getStartDateTime());
      truckStopDAO.addStops(stops);
    } catch (Exception e) {
      log.log(Level.WARNING, "Exception thrown while refreshing trucks", e);
    }
  }

  @Override
  public Set<TruckLocationGroup> findFoodTruckGroups(DateTime dateTime) {
    Multimap<Location, Truck> locations = LinkedListMultimap.create();
    Set<Truck> allTrucks = Sets.newHashSet();
    allTrucks.addAll(trucks.allTrucks());
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
    Truck truck = trucks.findById(truckId);
    if (truck == null) {
      throw new IllegalStateException("Invalid truck id specified: " + truckId);
    }
    return new TruckSchedule(truck, day, truckStopDAO.findDuring(truckId, day));
  }

  @Override
  public DailySchedule findStopsForDay(LocalDate day) {
    List<TruckStop> stops = truckStopDAO.findDuring(null, day);
    return new DailySchedule(stops);
  }

  @Override public List<TruckStatus> findCurrentAndPreviousStop(LocalDate day) {
    List<TruckStop> stops = truckStopDAO.findDuring(null, day);
    ImmutableList.Builder<TruckStatus> truckInfo = ImmutableList.builder();
    DateTime now = clock.now();
    for (final Truck truck : trucks.allTrucks()) {
      boolean activeToday = false;
      TruckStop currentStop = null;
      TruckStop nextStop = null;
      for (final TruckStop truckStop : stops) {
        if (truckStop.getTruck().getId().equals(truck.getId())) {
          activeToday = true;
          if (truckStop.activeDuring(now)) {
            currentStop = truckStop;
          } else if (truckStop.getStartTime().isAfter(now) &&
              (nextStop == null || truckStop.getStartTime().isBefore(nextStop.getStartTime()))) {
            nextStop = truckStop;
          }

        }
      }
      truckInfo.add(new TruckStatus(truck, activeToday, currentStop, nextStop));
    }
    return TruckStatus.BY_NAME.immutableSortedCopy(truckInfo.build());
  }
}
