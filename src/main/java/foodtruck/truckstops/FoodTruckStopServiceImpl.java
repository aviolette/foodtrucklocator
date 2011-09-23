package foodtruck.truckstops;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
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
import foodtruck.model.Trucks;
import foodtruck.schedule.GoogleCalendar;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.schedule.TwitterFeedSearch;

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
  private TwitterFeedSearch feedSearch;

  @Inject
  public FoodTruckStopServiceImpl(TruckStopDAO truckStopDAO, GoogleCalendar googleCalendar,
      Trucks trucks, DateTimeZone zone, TwitterFeedSearch feedSearch) {
    this.truckStopDAO = truckStopDAO;
    this.googleCalendar = googleCalendar;
    this.trucks = trucks;
    this.zone = zone;
    this.feedSearch = feedSearch;
  }

  @Override
  public void updateStopsFor(LocalDate instant) {
    TimeRange theDay = new TimeRange(instant, zone);
    truckStopDAO.deleteAfter(theDay.getStartDateTime());
    pullTruckSchedule(theDay);
  }

  private void pullTruckSchedule(TimeRange theDay) {
//    Multimap<String, TruckStopMatch> matches = feedSearch.findTweets(2);
    Multimap<String, TruckStopMatch> matches = HashMultimap.create();
    for (Truck truck : trucks.allTrucks()) {
      try {
        List<TruckStop> stops = googleCalendar.findForTime(truck, theDay);
        stops = alterStopsWithCurrentData(stops, matches.get(truck.getId()), truck);
        truckStopDAO.addStops(stops);
      } catch (Exception e) {
        log.log(Level.WARNING, "Exception thrown while refreshing truck: " + truck.getId(), e);
      }
    }
  }

  private List<TruckStop> alterStopsWithCurrentData(List<TruckStop> stops,
      Collection<TruckStopMatch> matches, Truck truck) {
    log.log(Level.INFO, "Matches for {0}: {1}", new Object[] {truck.getId(), matches});
    return stops;
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
}
