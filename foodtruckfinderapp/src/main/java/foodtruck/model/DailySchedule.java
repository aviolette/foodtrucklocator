package foodtruck.model;

import java.util.List;

/**
 * Represents all the truck stops for a given day.
 * @author aviolette@gmail.com
 * @since 12/7/11
 */
public class DailySchedule {
  private List<TruckStop> stops;

  public DailySchedule(List<TruckStop> stops) {
    this.stops = stops;
  }

  public List<TruckStop> getStops() {
    return stops;
  }
}
