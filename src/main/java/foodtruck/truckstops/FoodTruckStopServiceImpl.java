package foodtruck.truckstops;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.beaconnaise.BeaconSignal;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.MessageDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.model.DailySchedule;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckLocationGroup;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStatus;
import foodtruck.model.TruckStop;
import foodtruck.model.WeeklySchedule;
import foodtruck.schedule.ScheduleStrategy;
import foodtruck.util.Clock;
import foodtruck.util.ServiceException;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class FoodTruckStopServiceImpl implements FoodTruckStopService {
  private final TruckStopDAO truckStopDAO;
  private final TruckDAO truckDAO;
  private final ScheduleStrategy scheduleStrategy;
  private static final Logger log = Logger.getLogger(FoodTruckStopServiceImpl.class.getName());
  private final Clock clock;
  private final LocationDAO locationDAO;
  private final GeoLocator geolocator;
  private final MessageDAO messageDAO;

  @Inject
  public FoodTruckStopServiceImpl(TruckStopDAO truckStopDAO, ScheduleStrategy googleCalendar,
      Clock clock, TruckDAO truckDAO, LocationDAO locationDAO, GeoLocator geoLocator, MessageDAO messageDAO) {
    this.truckStopDAO = truckStopDAO;
    this.scheduleStrategy = googleCalendar;
    this.clock = clock;
    this.truckDAO = truckDAO;
    this.locationDAO = locationDAO;
    this.geolocator = geoLocator;
    this.messageDAO = messageDAO;
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
    return dailySchedule(day, stops);
  }

  private DailySchedule dailySchedule(LocalDate day, List<TruckStop> stops) {
    return new DailySchedule(day, stops, messageDAO.findByDay(day));
  }

  @Override public DailySchedule findStopsForDayAfter(final DateTime dateTime) {
    LocalDate day = dateTime.toLocalDate();
    List<TruckStop> stops = truckStopDAO.findDuring(null, day);
    return dailySchedule(day, FluentIterable.from(stops)
        .filter(new Predicate<TruckStop>() {
          @Override public boolean apply(TruckStop truckStop) {
            return truckStop.getEndTime().isAfter(dateTime);
          }
        })
        .toList());
  }

  @Override public TruckStop handleBeacon(BeaconSignal signal) {
    TruckStop matched = null, afterMatch = null;
    DateTime now = clock.now();
    Truck truck = null;
    for ( TruckStop stop : truckStopDAO.findDuring(signal.getTruckId(), clock.currentDay())) {
      if (truck == null) {
        truck = stop.getTruck();
      }
      if (matched != null || stop.getStartTime().isAfter(now)) {
        afterMatch = stop;
        break;
      }
      if (stop.activeDuring(now)) {
        matched = stop;
      }
    }

    final DateTime
        startTime = (matched == null) ? clock.now() : matched.getStartTime(),
        greaterEndTime = greaterOf(startTime.plusHours(2), clock.now().plusMinutes(5)),
        endTime = (afterMatch == null) ? greaterEndTime :
            lesserOf(greaterEndTime, afterMatch.getStartTime().plusMinutes(5));
    Location locWithName = signal.getLocation();

    TruckStop newStop;
    if (matched != null) {
      // same location
      if (matched.getLocation().within(0.01).milesOf(signal.getLocation())) {
        newStop = TruckStop.builder(matched).endTime(endTime).fromBeacon(clock.now()).build();
      } else {
        try {
          locWithName = geolocator.reverseLookup(signal.getLocation());
        } catch (ServiceException se) {
          log.log(Level.WARNING, se.getMessage(), se);
        }
        matched = TruckStop.builder(matched).endTime(clock.now()).build();
        truckStopDAO.save(matched);
        newStop = TruckStop.builder().truck(truck).startTime(clock.now()).endTime(endTime)
            .location(locWithName).fromBeacon(clock.now()).build();
      }
    } else {
      if (truck == null) {
        truck = truckDAO.findById(signal.getTruckId());
      }
      try {
        locWithName = geolocator.reverseLookup(signal.getLocation());
      } catch (ServiceException se) {
        log.log(Level.WARNING, se.getMessage(), se);
      }
      newStop = TruckStop.builder().truck(truck).startTime(startTime).endTime(endTime)
          .location(locWithName).fromBeacon(clock.now()).build();
    }
    truckStopDAO.save(newStop);
    return newStop;
  }

  @Override public void offRoad(String truckId, LocalDate localDate) {
    TruckSchedule stops = findStopsForDay(truckId, clock.currentDay());
    for (TruckStop stop : stops.getStops()) {
      delete((Long) stop.getKey());
    }
    Truck t = truckDAO.findById(truckId);
    t = Truck.builder(t)
        .muteUntil(clock.currentDay()
            .toDateMidnight(clock.zone()).toDateTime().plusDays(1))
        .build();
    truckDAO.save(t);
  }

  @Override public TruckStop findFirstStop(Truck truck) {
    return truckStopDAO.findFirstStop(truck.getId());
  }

  private DateTime lesserOf(DateTime dateTime1, DateTime dateTime2) {
    return dateTime1.isBefore(dateTime2) ? dateTime1 : dateTime2;
  }

  private DateTime greaterOf(DateTime dateTime1, DateTime dateTime2) {
    return (dateTime1.isBefore(dateTime2)) ? dateTime2 : dateTime1;
  }

  @Override public List<DailySchedule> findSchedules(String truckId, Interval range) {
    List<TruckStop> stopList = truckStopDAO.findOverRange(truckId, range);
    LocalDate date = range.getStart().toLocalDate();
    long numDays = range.toDuration().getStandardDays();
    Multimap<LocalDate, TruckStop> stopMM = ArrayListMultimap.create();
    for (TruckStop truckStop : stopList) {
      final LocalDate localDate = truckStop.getStartTime().toLocalDate();
      stopMM.put(localDate, truckStop);
    }
    ImmutableList.Builder<DailySchedule> stops = ImmutableList.builder();
    for (int i=0; i < numDays; i++) {
      LocalDate currentDay = date.plusDays(i);
      if (stopMM.containsKey(currentDay)) {
        stops.add(new DailySchedule(currentDay, ImmutableList.copyOf(stopMM.get(currentDay)), null));
      } else {
        stops.add(new DailySchedule(currentDay, ImmutableList.<TruckStop>of(), null));
      }
    }
    return stops.build();
  }

  @Override public Set<Truck> findTrucksAtLocation(LocalDate localDate, Location location) {
    ImmutableSet.Builder<Truck> builder = ImmutableSet.builder();
    for (TruckStop stop : truckStopDAO.findDuring(null, localDate)) {
      if (location.getName().equals(stop.getLocation().getName())) {
        builder.add(stop.getTruck());
      }
    }
    return builder.build();
  }

  @Override public Set<Truck> findTrucksNearLocation(Location location, DateTime currentTime) {
    ImmutableSet.Builder<Truck> builder = ImmutableSet.builder();
    Location existing = locationDAO.findByAddress(location.getName());
    location = existing == null ? location : existing;
    for (TruckStop stop : truckStopDAO.findDuring(null, currentTime.toLocalDate())) {
      if (stop.hasExpiredBy(currentTime)) {
        continue;
      }
      if (location.getName().equals(stop.getLocation().getName()) ||
          location.within(location.getRadius()).milesOf(stop.getLocation())) {
        builder.add(stop.getTruck());
      }
    }
    return builder.build();
  }

  @Override public List<TruckStop> findStopsNearALocation(Location location, LocalDate theDate) {
    ImmutableList.Builder<TruckStop> builder = ImmutableList.builder();
    for (TruckStop stop : truckStopDAO.findDuring(null, theDate)) {
      if (location.getName().equals(stop.getLocation().getName()) ||
          location.within(location.getRadius()).milesOf(stop.getLocation())) {
        builder.add(stop);
      }
    }
    return builder.build();
  }

  @Override public List<TruckStop> findStopsForTruckSince(DateTime since, String truckId) {
    return truckStopDAO.findOverRange(truckId, new Interval(since, clock.now()));
  }

  @Override public WeeklySchedule findPopularStopsForWeek(LocalDate startDate) {
    List<TruckStop> stops = truckStopDAO.findOverRange(null, new Interval(startDate.toDateTimeAtStartOfDay(),
        startDate.plusDays(7).toDateTimeAtStartOfDay()));
    Set<Location> locationSet = locationDAO.findPopularLocations();
    Map<String, Location> locationMap = Maps.newHashMap();
    for (Location loc : locationSet) {
      locationMap.put(loc.getName(), loc);
    }
    WeeklySchedule.Builder scheduleBuilder = new WeeklySchedule.Builder();
    scheduleBuilder.start(startDate);
    for (TruckStop stop : stops) {
      if (locationMap.containsKey(stop.getLocation().getName())) {
        scheduleBuilder.addStop(stop);
      } else {
        for (Location loc : locationSet) {
          if (loc.within(loc.getRadius()).milesOf(stop.getLocation())) {
            scheduleBuilder.addStop(stop.withLocation(loc));
            break;
          }
        }
      }
    }
    return scheduleBuilder.build();
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
