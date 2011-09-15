package foodtruck.model;

import java.util.List;

import org.joda.time.LocalDate;

/**
 * A schedule for a truck on a day
 * @author aviolette@gmail.com
 * @since 9/14/11
 */
public class TruckSchedule {
  private final List<TruckStop> stops;
  private final Truck truck;
  private final LocalDate date;

  public TruckSchedule(Truck truck, LocalDate date, List<TruckStop> stops) {
    this.truck = truck;
    this.date = date;
    this.stops = stops;
  }

  public List<TruckStop> getStops() {
    return stops;
  }

  public Truck getTruck() {
    return truck;
  }

  public LocalDate getDate() {
    return date;
  }
}
