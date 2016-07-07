package foodtruck.truckstops;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.beaconnaise.BeaconSignal;
import foodtruck.dao.DailyDataDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.MessageDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.model.DailySchedule;
import foodtruck.model.Location;
import foodtruck.model.StopOrigin;
import foodtruck.model.Truck;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStatus;
import foodtruck.model.TruckStop;
import foodtruck.model.TruckStopWithCounts;
import foodtruck.model.WeeklySchedule;
import foodtruck.schedule.ScheduleStrategy;
import foodtruck.util.Clock;
import foodtruck.util.ServiceException;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class FoodTruckStopServiceImpl implements FoodTruckStopService {
  private static final Logger log = Logger.getLogger(FoodTruckStopServiceImpl.class.getName());
  private final TruckStopDAO truckStopDAO;
  private final TruckDAO truckDAO;
  private final ScheduleStrategy scheduleStrategy;
  private final Clock clock;
  private final LocationDAO locationDAO;
  private final GeoLocator geolocator;
  private final MessageDAO messageDAO;
  private final DailyDataDAO dailyDataDAO;

  @Inject
  public FoodTruckStopServiceImpl(TruckStopDAO truckStopDAO, ScheduleStrategy googleCalendar, Clock clock,
      TruckDAO truckDAO, LocationDAO locationDAO, GeoLocator geoLocator, MessageDAO messageDAO,
      DailyDataDAO dailyDataDAO) {
    this.truckStopDAO = truckStopDAO;
    this.scheduleStrategy = googleCalendar;
    this.clock = clock;
    this.truckDAO = truckDAO;
    this.locationDAO = locationDAO;
    this.geolocator = geoLocator;
    this.messageDAO = messageDAO;
    this.dailyDataDAO = dailyDataDAO;
  }

  @Override
  public void pullCustomCalendarFor(Interval range, Truck truck) {
    List<TruckStop> stops = scheduleStrategy.findForTime(range, truck);
    truckStopDAO.deleteStops(truckStopDAO.findVendorStopsAfter(range.getStart(), truck.getId()));
    truckStopDAO.addStops(stops);
  }

  @Override public @Nullable TruckStop findById(long stopId) {
    return truckStopDAO.findById(stopId);
  }

  @Override public void delete(long stopId) {
    truckStopDAO.delete(stopId);
  }

  @Override public void update(TruckStop truckStop, String modifier) {
    if (truckStop.isNew()) {
      truckStop = TruckStop.builder(truckStop)
          .notes(ImmutableList.of("Entered manually by " + modifier + " at " + clock.nowFormattedAsTime())).build();
    } else {
      TruckStop stop = truckStopDAO.findById((Long) truckStop.getKey());
      truckStop = TruckStop.builder(truckStop)
          .notes(stop.getNotes())
          .origin(stop.getOrigin())
          .appendNote("Changed manually by " + modifier + " at " + clock.nowFormattedAsTime()).build();
    }
    truckStopDAO.save(truckStop);
  }

  @Override
  public void pullCustomCalendars(Interval theDay) {
    try {
      List<TruckStop> stops = scheduleStrategy.findForTime(theDay, null);
      List<TruckStop> vendorStopsAfter = truckStopDAO.findVendorStopsAfter(theDay.getStart());
      log.log(Level.INFO, "Removing stops: " + vendorStopsAfter.size());
      truckStopDAO.deleteStops(vendorStopsAfter);
      truckStopDAO.addStops(stops);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Exception thrown while refreshing trucks", e);
    }
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
    return DailySchedule.builder()
        .date(day)
        .stops(stops)
        .message(messageDAO.findByDay(day))
        .specials(ImmutableSet.copyOf(dailyDataDAO.findTruckSpecialsByDay(day)))
        .build();
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
        newStop = TruckStop.builder().origin(StopOrigin.BEACONNAISE)
            .truck(truck).startTime(clock.now()).endTime(endTime)
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
      newStop = TruckStop.builder()
          .origin(StopOrigin.BEACONNAISE)
          .truck(truck).startTime(startTime).endTime(endTime)
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
            .toDateTimeAtStartOfDay(clock.zone()).plusDays(1))
        .build();
    truckDAO.save(t);
  }

  @Override public int cancelRemainingStops(String truckId, DateTime after) {
    TruckSchedule stops = findStopsForDay(truckId, clock.currentDay());
    int count = 0;
    for (TruckStop stop : stops.getStops()) {
      if (stop.activeDuring(after)) {
        update(stop.withEndTime(after), "unknown");
        count++;
      } else if (stop.getEndTime().isAfter(after)) {
        delete((Long) stop.getKey());
        count++;
      }
    }
    Truck t = truckDAO.findById(truckId);
    t = Truck.builder(t)
        .muteUntil(clock.currentDay()
            .toDateTimeAtStartOfDay(clock.zone()).plusDays(1))
        .build();
    truckDAO.save(t);
    return count;
  }

  @Override public TruckStop findFirstStop(Truck truck) {
    return truckStopDAO.findFirstStop(truck.getId());
  }

  @Override public List<TruckStop> findUpcomingBoozyStops(LocalDate startDate, int daysOut) {
    final Map<String, Location> locations = Maps.newHashMap();
    for (Location loc : locationDAO.findBoozyLocations()) {
      log.log(Level.INFO, "Boozy location {0}", loc);
      locations.put(loc.getName(), loc);
    }
    return FluentIterable.from(truckStopDAO.findOverRange(null, new Interval(startDate.toDateTimeAtStartOfDay(clock.zone()),
        startDate.plusDays(daysOut).toDateTimeAtStartOfDay(clock.zone()))))
        .filter(new Predicate<TruckStop>() {
          public boolean apply(TruckStop truckStop) {
            return locations.containsKey(truckStop.getLocation().getName());
          }
        })
        .toList();
  }

  @Override
  public List<TruckStopWithCounts> findStopsForTruckAfter(final String truckId, DateTime startTime) {
    final List<TruckStop> stops = truckStopDAO.findAfter(startTime);
    return FluentIterable.from(stops)
        .filter(new Predicate<TruckStop>() {
          public boolean apply(@Nullable TruckStop truckStop) {
            return truckId.equals(truckStop.getTruck().getId());
          }
        })
        .transform(new Function<TruckStop, TruckStopWithCounts>() {
          public TruckStopWithCounts apply(final TruckStop thisStop) {
            ImmutableSet<String> trucks = ImmutableSet.of();
            try {
              final Interval thisInterval = thisStop.timeInterval();
              trucks = FluentIterable.from(stops).filter(new Predicate<TruckStop>() {
                public boolean apply(TruckStop truckStop) {
                  try {
                    return truckStop.timeInterval().overlap(thisInterval) != null && truckStop.getLocation()
                        .getName()
                        .equals(thisStop.getLocation().getName());
                  } catch (RuntimeException rte) {
                    log.log(Level.WARNING, "Error processing stop {0}", truckStop);
                    throw rte;
                  }
                }
              }).transform(new Function<TruckStop, String>() {
                public String apply(TruckStop truckStop) {
                  return truckStop.getTruck().getName();
                }
              }).toSet();
            } catch (RuntimeException e) {
              log.log(Level.SEVERE, e.getMessage(), e);
            } return new TruckStopWithCounts(thisStop, trucks);
          }
        })
        .toList();
  }

  @Override
  public List<DailySchedule> findStopsNearLocationOverRange(final Location location, Interval range) {
    ImmutableList.Builder<DailySchedule> builder = ImmutableList.builder();
    DailySchedule.Builder schedule = null;
    for (TruckStop stop : truckStopDAO.findOverRange(null, range)) {
      if (!stop.getLocation().containedWithRadiusOf(location)) {
        continue;
      }
      if (schedule != null && !schedule.getDay().isEqual(stop.getStartTime().toLocalDate())) {
        builder.add(schedule.build());
        schedule = null;
      }
      if (schedule == null) {
        schedule = DailySchedule.builder()
            .date(stop.getStartTime().toLocalDate());
      }
      schedule.addStop(stop);
    }
    if (schedule != null && schedule.hasStops()) {
      builder.add(schedule.build());
    }
    return builder.build();
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
      DailySchedule.Builder builder = DailySchedule.builder().date(currentDay);
      if (stopMM.containsKey(currentDay)) {
        stops.add(builder.stops(ImmutableList.copyOf(stopMM.get(currentDay))).build());
      } else {
        stops.add(builder.build());
      }
    }
    return stops.build();
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

  @Override public List<TruckStop> findStopsForTruckSince(DateTime since, @Nullable String truckId) {
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
      if (stop.getTruck() == null) {
        // this will happen if I deleted a truck that was referenced in a stop
        continue;
      }
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
