package foodtruck.model;

import java.util.List;

import org.joda.time.LocalDate;

/**
 * Represents all the truck stops for a given day.
 * @author aviolette@gmail.com
 * @since 12/7/11
 */
public class DailySchedule {
  private final List<TruckStop> stops;
  private final LocalDate day;

  public DailySchedule(LocalDate day, List<TruckStop> stops) {
    this.stops = stops;
    this.day = day;
  }

  public LocalDate getDay() {
    return this.day;
  }

  public List<TruckStop> getStops() {
    return stops;
  }
}
