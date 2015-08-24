package foodtruck.model;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.joda.time.DateTime;

/**
 * @author aviolette
 * @since 5/28/13
 */
public class Event extends ModelEntity {
  private final Location location;
  private final DateTime startTime;
  private final DateTime endTime;
  private final @Nullable String url;
  private final List<Truck> trucks;
  private final String description;
  private final String name;

  private Event(Builder b) {
    super(b.key);
    this.location = b.location;
    this.startTime = b.startTime;
    this.endTime = b.endTime;
    this.url = b.url;
    this.trucks = b.trucks;
    this.description = b.description;
    this.name = b.name;
  }

  public Location getLocation() {
    return location;
  }

  public DateTime getStartTime() {
    return startTime;
  }

  public DateTime getEndTime() {
    return endTime;
  }

  public boolean isStartAndEndOnSameDay() {
    return startTime.toLocalDate().equals(endTime.toLocalDate());
  }

  public String getName() {
    return name;
  }

  public @Nullable String getUrl() {
    return url;
  }

  public List<Truck> getTrucks() {
    return trucks;
  }

  public String getTruckIds() {
    return Joiner.on(", ").join(Iterables.transform(trucks, Truck.TO_ID));
  }

  public String getDescription() {
    return description;
  }

  public static Builder builder() {
    return new Builder();
  }

  private Object[] significantAttributes() {
    return new Object[]{location, startTime, endTime, trucks, name};
  }

  @Override public int hashCode() {
    return Objects.hashCode(significantAttributes());
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Event)) {
      return false;
    }
    return Arrays.equals(((Event) o).significantAttributes(), significantAttributes());
  }

  @Override public String toString() {
    return super.toString();
  }

  public static class Builder {
    private Location location;
    private DateTime startTime;
    private DateTime endTime;
    private @Nullable String url;
    private List<Truck> trucks = ImmutableList.of();
    private String description;
    private String key;
    private String name;

    private Builder() {
    }

    public Builder key(String key) {
      this.key = key;
      return this;
    }

    public Builder location(Location location) {
      this.location = location;
      return this;
    }

    public Builder startTime(DateTime dateTime) {
      this.startTime = dateTime;
      return this;
    }

    public Builder endTime(DateTime dateTime) {
      this.endTime = dateTime;
      return this;
    }

    public Builder url(@Nullable String url) {
      this.url = url;
      return this;
    }

    public Builder trucks(List<Truck> trucks) {
      this.trucks = trucks;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Event build() {
      return new Event(this);
    }
  }
}
