package foodtruck.schedule;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.dao.DailyDataDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.MessageDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.DailySchedule;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStatus;
import foodtruck.model.TruckStop;
import foodtruck.model.TruckStopWithCounts;
import foodtruck.time.Clock;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
class FoodTruckStopServiceImpl implements FoodTruckStopService {
  private static final Logger log = Logger.getLogger(FoodTruckStopServiceImpl.class.getName());
  private final TruckStopDAO truckStopDAO;
  private final TruckDAO truckDAO;
  private final Set<ScheduleStrategy> calendars;
  private final Clock clock;
  private final LocationDAO locationDAO;
  private final MessageDAO messageDAO;
  private final DailyDataDAO dailyDataDAO;

  @Inject
  public FoodTruckStopServiceImpl(TruckStopDAO truckStopDAO, Set<ScheduleStrategy> calendarConnectors, Clock clock,
      TruckDAO truckDAO, LocationDAO locationDAO, MessageDAO messageDAO, DailyDataDAO dailyDataDAO) {
    this.truckStopDAO = truckStopDAO;
    this.calendars = calendarConnectors;
    this.clock = clock;
    this.truckDAO = truckDAO;
    this.locationDAO = locationDAO;
    this.messageDAO = messageDAO;
    this.dailyDataDAO = dailyDataDAO;
  }

  @Override
  public void pullCustomCalendarFor(Interval range, Truck truck) {
    List<TruckStop> stops = calendars.stream()
        .map(scheduleStrategy -> scheduleStrategy.findForTime(range, truck))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    if ("mytoastycheese".equals(truck.getId())) {
      // HACK: these two trucks share the same calendar
      truckStopDAO.deleteStops(truckStopDAO.findVendorStopsAfter(range.getStart(), "besttruckinbbq"));
      truckStopDAO.deleteStops(truckStopDAO.findVendorStopsAfter(range.getStart(), "mytoastytaco"));
      truckStopDAO.deleteStops(truckStopDAO.findVendorStopsAfter(range.getStart(), "thecravebar"));
    }
    truckStopDAO.deleteStops(truckStopDAO.findVendorStopsAfter(range.getStart(), truck.getId()));
    truckStopDAO.addStops(stops);
  }

  @Override
  public void delete(long stopId) {
    truckStopDAO.delete(stopId);
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void update(TruckStop truckStop, String modifier) {
    if (truckStop.isNew()) {
      truckStop = TruckStop.builder(truckStop)
          .manuallyUpdated(clock.now())
          .notes(ImmutableList.of("Entered manually by " + modifier + " at " + clock.nowFormattedAsTime()))
          .build();
    } else {
      Long stopKey = (Long) truckStop.getKey();
      TruckStop stop = truckStopDAO.findByIdOpt((Long) truckStop.getKey())
          .orElseThrow(() -> new IllegalStateException("Invalid ID: " + stopKey));
      truckStop = TruckStop.builder(truckStop)
          .notes(stop.getNotes())
          .origin(stop.getOrigin())
          .fromBeacon(stop.getBeaconTime())
          .createdWithDeviceId(stop.getCreatedWithDeviceId())
          .manuallyUpdated(clock.now())
          .appendNote("Changed manually by " + modifier + " at " + clock.nowFormattedAsTime())
          .build();
    }
    truckStopDAO.save(truckStop);
  }

  @Override
  public void pullCustomCalendars(Interval theDay) {
    try {
      List<TruckStop> stops = calendars.stream()
          .map(scheduleStrategy -> scheduleStrategy.findForTime(theDay, null))
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
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
    Truck truck = truckDAO.findByIdOpt(truckId)
        .orElseThrow(() -> new IllegalStateException("Not found: " + truckId));
    return new TruckSchedule(truck, day, truckStopDAO.findDuring(truckId, day));
  }

  @Override
  public DailySchedule findStopsForDay(LocalDate day) {
    return dailySchedule(day, truckStopDAO.findDuring(null, day));
  }

  private DailySchedule dailySchedule(LocalDate day, List<TruckStop> stops) {
    return DailySchedule.builder()
        .date(day)
        .stops(stops)
        .message(messageDAO.findByDay(day))
        .specials(ImmutableSet.copyOf(dailyDataDAO.findTruckSpecialsByDay(day)))
        .build();
  }

  @Override
  public DailySchedule findStopsForDayAfter(final DateTime dateTime) {
    LocalDate day = dateTime.toLocalDate();
    List<TruckStop> stops = truckStopDAO.findDuring(null, day);
    return dailySchedule(day, stops.stream()
        .filter(truckStop -> truckStop.getEndTime().isAfter(dateTime))
        .collect(Collectors.toList()));
  }

  @Override
  public void offRoad(String truckId, LocalDate localDate) {
    TruckSchedule stops = findStopsForDay(truckId, clock.currentDay());
    for (TruckStop stop : stops.getStops()) {
      delete((Long) stop.getKey());
    }
    truckDAO.findByIdOpt(truckId).ifPresent(truck -> truckDAO.save(Truck.builder(truck)
        .muteUntil(clock.currentDay()
            .toDateTimeAtStartOfDay(clock.zone())
            .plusDays(1))
        .build()));
  }

  @Override
  public int cancelRemainingStops(String truckId, DateTime after) {
    TruckSchedule stops = findStopsForDay(truckId, clock.currentDay());
    int count = 0;
    for (TruckStop stop : stops.getStops()) {
      if (stop.activeDuring(after)) {
        update(stop.withEndTime(after), "unknown");
        count++;
      } else if (stop.getEndTime()
          .isAfter(after)) {
        delete((Long) stop.getKey());
        count++;
      }
    }
    truckDAO.findByIdOpt(truckId)
        .ifPresent(truck -> truckDAO.save(Truck.builder(truck)
            .muteUntil(clock.currentDay()
                .toDateTimeAtStartOfDay(clock.zone())
                .plusDays(1))
            .build()));
    return count;
  }

  @Override
  public TruckStop findFirstStop(Truck truck) {
    return truckStopDAO.findFirstStop(truck.getId());
  }

  @Override
  public List<TruckStop> findUpcomingBoozyStops(LocalDate startDate, int daysOut) {
    final Map<String, Location> locations = Maps.newHashMap();
    for (Location loc : locationDAO.findBoozyLocations()) {
      log.log(Level.INFO, "Boozy location {0}", loc);
      locations.put(loc.getName(), loc);
    }
    return truckStopDAO.findOverRange(null, new Interval(startDate.toDateTimeAtStartOfDay(clock.zone()), startDate.plusDays(daysOut)
            .toDateTimeAtStartOfDay(clock.zone()))).stream()
        .filter(truckStop -> locations.containsKey(truckStop.getLocation()
            .getName()))
        .collect(Collectors.toList());
  }

  @Override
  public List<TruckStop> findStopsForTruckAfterWithoutCounts(String truckId, DateTime startTime) {
    return truckStopDAO.findAfter(truckId, startTime);
  }

  @Override
  public List<TruckStopWithCounts> findStopsForTruckAfter(final String truckId, DateTime startTime) {
    List<TruckStop> stops = truckStopDAO.findAfter(startTime);
    return stops.stream()
        .filter(truckStop -> truckId.equals(truckStop.getTruck().getId()))
        .map(truckStop -> truckStop.overlapWithCounts(stops))
        .collect(Collectors.toList());
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
        schedule = DailySchedule.builder().date(stop.getStartTime().toLocalDate());
      }
      schedule.addStop(stop);
    }
    if (schedule != null && schedule.hasStops()) {
      builder.add(schedule.build());
    }
    return builder.build();
  }

  @Override
  public List<DailySchedule> findSchedules(String truckId, Interval range) {
    List<TruckStop> stopList = truckStopDAO.findOverRange(truckId, range);
    LocalDate date = range.getStart()
        .toLocalDate();
    long numDays = range.toDuration()
        .getStandardDays();
    Multimap<LocalDate, TruckStop> stopMM = ArrayListMultimap.create();
    for (TruckStop truckStop : stopList) {
      final LocalDate localDate = truckStop.getStartTime()
          .toLocalDate();
      stopMM.put(localDate, truckStop);
    }
    ImmutableList.Builder<DailySchedule> stops = ImmutableList.builder();
    for (int i = 0; i < numDays; i++) {
      LocalDate currentDay = date.plusDays(i);
      DailySchedule.Builder builder = DailySchedule.builder()
          .date(currentDay);
      if (stopMM.containsKey(currentDay)) {
        stops.add(builder.stops(ImmutableList.copyOf(stopMM.get(currentDay)))
            .build());
      } else {
        stops.add(builder.build());
      }
    }
    return stops.build();
  }

  @Override
  public Set<Truck> findTrucksNearLocation(Location location, DateTime currentTime) {
    final Location center = locationDAO.findByName(location.getName()).orElse(location);
    return truckStopDAO.findDuring(null, currentTime.toLocalDate())
        .stream()
        .filter(stop -> !stop.hasExpiredBy(currentTime)
            && (center.getName().equals(stop.getLocation().getName())
              || center.within(center.getRadius()).milesOf(stop.getLocation())))
        .map(TruckStop::getTruck)
        .collect(Collectors.toSet());
  }

  @Override
  public List<TruckStop> findStopsNearALocation(Location location, LocalDate theDate) {
    ImmutableList.Builder<TruckStop> builder = ImmutableList.builder();
    for (TruckStop stop : truckStopDAO.findDuring(null, theDate)) {
      if (location.getName()
          .equals(stop.getLocation()
              .getName()) || location.within(location.getRadius())
          .milesOf(stop.getLocation())) {
        builder.add(stop);
      }
    }
    return builder.build();
  }

  @Override
  public List<TruckStop> findStopsForTruckSince(DateTime since, @Nullable String truckId) {
    return truckStopDAO.findOverRange(truckId, new Interval(since, clock.now()));
  }


  @Override
  public List<TruckStatus> findCurrentAndPreviousStop(LocalDate day) {
    List<TruckStop> stops = truckStopDAO.findDuring(null, day);
    ImmutableList.Builder<TruckStatus> truckInfo = ImmutableList.builder();
    DateTime now = clock.now();
    for (final Truck truck : truckDAO.findAll()) {
      boolean activeToday = false;
      TruckStop currentStop = null;
      TruckStop nextStop = null;
      for (final TruckStop truckStop : stops) {
        if (truckStop.getTruck()
            .getId()
            .equals(truck.getId())) {
          activeToday = true;
          if (truckStop.activeDuring(now)) {
            currentStop = truckStop;
          } else if (truckStop.getStartTime()
              .isAfter(now) && (nextStop == null || truckStop.getStartTime()
              .isBefore(nextStop.getStartTime()))) {
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
      if (stop.getLocation()
          .getName()
          .equals(location.getName())) {
        stop = stop.withLocation(location);
        truckStopDAO.save(stop);
      }
    }
  }
}
