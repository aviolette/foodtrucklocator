package foodtruck.model;

import java.util.List;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;

/**
 * Represents all the truck stops for a given day.
 * @author aviolette@gmail.com
 * @since 12/7/11
 */
public class DailySchedule {
  private final List<TruckStop> stops;
  private final LocalDate day;
  private final Message message;

  public DailySchedule(LocalDate day, List<TruckStop> stops, @Nullable Message messageOfTheDay) {
    this.stops = stops;
    this.day = day;
    this.message = messageOfTheDay;
  }

  public @Nullable Message getMessageOfTheDay() {
    return message;
  }

  public LocalDate getDay() {
    return this.day;
  }

  public List<TruckStop> getStops() {
    return stops;
  }

  public boolean isHasStops() {
    return !stops.isEmpty();
  }
}
