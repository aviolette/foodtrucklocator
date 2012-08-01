package foodtruck.truckstops;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.DailySchedule;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckLocationGroup;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStatus;
import foodtruck.model.TruckStop;
import foodtruck.schedule.ScheduleStrategy;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class FoodTruckStopServiceImpl implements FoodTruckStopService {
  private final TruckStopDAO truckStopDAO;
  private final TruckDAO truckDAO;
  private final ScheduleStrategy scheduleStrategy;
  private static final Logger log = Logger.getLogger(FoodTruckStopServiceImpl.class.getName());
  private final DateTimeZone zone;
  private final Clock clock;

  @Inject
  public FoodTruckStopServiceImpl(TruckStopDAO truckStopDAO, ScheduleStrategy googleCalendar,
      DateTimeZone zone, Clock clock, TruckDAO truckDAO) {
    this.truckStopDAO = truckStopDAO;
    this.scheduleStrategy = googleCalendar;
    this.zone = zone;
    this.clock = clock;
    this.truckDAO = truckDAO;
  }

  @Override
  public void updateStopsForTruck(Interval range, Truck truck) {
    List<TruckStop> stops = scheduleStrategy.findForTime(range, truck);
    truckStopDAO.deleteAfter(range.getStart(), truck.getId());
    truckStopDAO.addStops(stops);
  }

  @Override public @Nullable TruckStop findById(long stopId) {
    return truckStopDAO.findById(stopId);
  }

  @Override public void delete(long stopId) {
    truckStopDAO.delete(stopId);
  }

  @Override public void update(TruckStop truckStop) {
    truckStopDAO.save(truckStop);
  }

  @Override
  public void updateStopsFor(Interval instant) {
    pullTruckSchedule(instant);
  }

  private void pullTruckSchedule(Interval theDay) {
    try {
      List<TruckStop> stops = scheduleStrategy.findForTime(theDay, null);
      truckStopDAO.deleteAfter(theDay.getStart());
      truckStopDAO.addStops(stops);
    } catch (Exception e) {
      log.log(Level.WARNING, "Exception thrown while refreshing trucks", e);
    }
  }

  @Override
  public Set<TruckLocationGroup> findFoodTruckGroups(DateTime dateTime) {
    Multimap<Location, Truck> locations = LinkedListMultimap.create();
    Set<Truck> allTrucks = Sets.newHashSet();
    allTrucks.addAll(truckDAO.findAll());
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
    Truck truck = truckDAO.findById(truckId);
    if (truck == null) {
      throw new IllegalStateException("Invalid truck id specified: " + truckId);
    }
    return new TruckSchedule(truck, day, truckStopDAO.findDuring(truckId, day));
  }

  @Override
  public DailySchedule findStopsForDay(LocalDate day) {
    List<TruckStop> stops = truckStopDAO.findDuring(null, day);
    return new DailySchedule(day, stops);
  }

  @Override public List<DailySchedule> findSchedules(String truckId, DateTime start, DateTime end) {
    List<TruckStop> stopList = truckStopDAO.findOverRange(truckId, start, end);
    ImmutableList.Builder<DailySchedule> stops = ImmutableList.builder();
    LocalDate date = start.toLocalDate();
    ImmutableList.Builder<TruckStop> currentStops = ImmutableList.builder();
    for (TruckStop truckStop : stopList) {
      final LocalDate localDate = truckStop.getStartTime().toLocalDate();
      if (!date.equals(localDate)) {
        ImmutableList<TruckStop> cs = currentStops.build();
        if (!cs.isEmpty()) {
          stops.add(new DailySchedule(date, cs));
        }
        currentStops = ImmutableList.builder();
        date = localDate;
      }
      currentStops.add(truckStop);
    }
    stops.add(new DailySchedule(date, currentStops.build()));
    return stops.build();
  }

  @Override public List<TruckStatus> findCurrentAndPreviousStop(LocalDate day) {
    List<TruckStop> stops = truckStopDAO.findDuring(null, day);
    ImmutableList.Builder<TruckStatus> truckInfo = ImmutableList.builder();
    DateTime now = clock.now();
    for (final Truck truck : truckDAO.findAll()) {
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

  @Override
  public void updateLocationInCurrentSchedule(Location location) {
    List<TruckStop> stops = truckStopDAO.findDuring(null, clock.currentDay());
    for (TruckStop stop : stops) {
      if (stop.getLocation().getName().equals(location.getName())) {
        stop = stop.withLocation(location);
        truckStopDAO.save(stop);
      }
    }
  }
}
