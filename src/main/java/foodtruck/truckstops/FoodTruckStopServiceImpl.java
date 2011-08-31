package foodtruck.truckstops;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import foodtruck.dao.TruckStopDAO;
import foodtruck.model.TimeRange;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.DefaultStrategy;
import foodtruck.schedule.ScheduleStrategy;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class FoodTruckStopServiceImpl implements FoodTruckStopService {
  private final TruckStopDAO truckStopDAO;
  private final Map<Truck, ScheduleStrategy> strategies;
  private final ScheduleStrategy defaultStrategy;
  private final Map<String, Truck> trucks;

  @Inject
  public FoodTruckStopServiceImpl(TruckStopDAO truckStopDAO, Map<Truck,
      ScheduleStrategy> strategies, @DefaultStrategy ScheduleStrategy defaultStrategy,
      Map<String, Truck> trucks) {
    this.truckStopDAO = truckStopDAO;
    this.strategies = strategies;
    this.defaultStrategy = defaultStrategy;
    this.trucks = trucks;
  }

  @Override
  public Set<TruckStop> findStopsFor(DateTime instant) {
    return truckStopDAO.findAt(instant);
  }

  @Override
  public void updateStopsFor(LocalDate instant) {
    TimeRange theDay = new TimeRange(instant);
    truckStopDAO.deleteAfter(theDay.getStartDateTime());
    for (Truck truck : trucks.values()) {
      ScheduleStrategy strategy = strategies.get(truck);
      if (strategy == null) {
        strategy = defaultStrategy;
      }
      List<TruckStop> stops = strategy.findForTime(truck, theDay);
      truckStopDAO.addStops(stops);
    }
  }

  @Override
  public Set<Truck> findTrucks() {
    Set<Truck> truckSet = Sets.newTreeSet(new Comparator<Truck>() {
      @Override
      public int compare(Truck t1, Truck t2) {
        return t1.getName().compareTo(t2.getName());
      }
    });
    truckSet.addAll(trucks.values());
    return truckSet;
  }
}
