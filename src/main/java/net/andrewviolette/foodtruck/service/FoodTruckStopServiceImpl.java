package net.andrewviolette.foodtruck.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import net.andrewviolette.foodtruck.dao.TruckStopDAO;
import net.andrewviolette.foodtruck.model.TimeRange;
import net.andrewviolette.foodtruck.model.Truck;
import net.andrewviolette.foodtruck.model.TruckStop;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class FoodTruckStopServiceImpl implements FoodTruckStopService {
  private final TruckStopDAO truckStopDAO;
  private final ConcurrentMap<String, ScheduleStrategy> strategies;
  private final ScheduleStrategy defaultStrategy;
  private final Map<String, Truck> trucks;

  @Inject
  public FoodTruckStopServiceImpl(TruckStopDAO truckStopDAO, ConcurrentMap<String,
      ScheduleStrategy> strategies, @DefaultStrategy ScheduleStrategy defaultStrategy,
      Map<String, Truck> trucks) {
    this.truckStopDAO = truckStopDAO;
    this.strategies = strategies;
    this.defaultStrategy = defaultStrategy;
    this.trucks = trucks;
  }

  @Override
  public Set<TruckStop> findStopsFor(DateTime instant) {
    return truckStopDAO.findAfter(instant);
  }

  @Override
  public void updateStopsFor(LocalDate instant) {
    TimeRange theDay = new TimeRange(instant);
    truckStopDAO.deleteAfter(theDay.getStartDateTime());
    for (Truck truck : trucks.values()) {
      ScheduleStrategy strategy = strategies.get(truck.getId());
      if (strategy == null) {
        strategy = defaultStrategy;
      }
      List<TruckStop> stops = strategy.findForTime(truck, theDay);
      truckStopDAO.addStops(stops);
    }
  }
}
