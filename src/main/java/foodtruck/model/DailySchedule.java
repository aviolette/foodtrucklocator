package foodtruck.model;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.joda.time.LocalDate;

/**
 * Represents all the truck stops for a given day.
 * @author aviolette@gmail.com
 * @since 12/7/11
 */
public class DailySchedule {
  private final ImmutableList<TruckStop> stops;
  private final LocalDate day;
  private @Nullable final Message message;
  private final ImmutableSet<DailyData> specials;

  private DailySchedule(Builder builder) {
    this.stops = ImmutableList.copyOf(builder.stops);
    this.day = builder.day;
    this.message = builder.message;
    this.specials = ImmutableSet.copyOf(builder.specials);
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

  public Set<DailyData> getSpecials() {
    return specials;
  }

  public boolean isAfterToday() {
    // TODO: this is kinda crappy since it doesn't take into effect time zones
    return day.isAfter(new LocalDate());
  }

  public boolean isHasStops() {
    return !stops.isEmpty();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private List<TruckStop> stops = ImmutableList.of();
    private LocalDate day;
    private @Nullable Message message;
    private Set<DailyData> specials = ImmutableSet.of();

    public Builder() {}

    public Builder stops(List<TruckStop> stops) {
      this.stops = stops;
      return this;
    }

    public Builder date(LocalDate date) {
      this.day = date;
      return this;
    }

    public Builder message(Message message) {
      this.message = message;
      return this;
    }

    public Builder specials(Set<DailyData> specials) {
      this.specials = specials;
      return this;
    }

    public DailySchedule build() {
      return new DailySchedule(this);
    }
  }

}
