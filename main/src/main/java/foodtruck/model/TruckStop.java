package foodtruck.model;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Specifies an truck at a location at a date and time.
 *
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class TruckStop extends ModelEntity {
  public static final Function TO_TRUCK_NAME = new Function<TruckStop, String>() {
    public String apply(TruckStop input) {
      return input.getTruck().getNameInSSML();
    }
  };
  public static final Function<TruckStop, String> TO_LOCATION_NAME = new Function<TruckStop, String>() {
    @Override
    public String apply(TruckStop input) {
      return input.getLocation()
          .getShortenedName();
    }
  };
  public static final Function<TruckStop, String> TO_NAME_WITH_TIME = new Function<TruckStop, String>() {
    private final DateTimeFormatter formatter = DateTimeFormat.forStyle("-S");

    @Override
    public String apply(TruckStop input) {
      return input.getLocation()
          .getShortenedName() + " at " + formatter.print(input.getStartTime());
    }
  };
  private static final Logger log = Logger.getLogger(TruckStop.class.getName());
  private final Truck truck;
  private final DateTime startTime;
  private final DateTime endTime;
  private final Location location;
  private final boolean locked;
  private final @Nullable DateTime lastUpdated;
  private final @Nullable DateTime fromBeacon;
  private final List<String> notes;
  private final StopOrigin origin;
  private final @Nullable Long createdWithDeviceId;

  private TruckStop(Builder builder) {
    super(builder.key);
    truck = builder.truck;
    startTime = builder.startTime;
    endTime = builder.endTime;
    location = builder.location;
    locked = builder.locked;
    fromBeacon = builder.fromBeacon;
    lastUpdated = builder.lastUpdated;
    notes = ImmutableList.copyOf(builder.notes);
    origin = builder.origin;
    createdWithDeviceId = builder.createdWithDeviceId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(TruckStop stop) {
    return new Builder(stop);
  }

  public Long getCreatedWithDeviceId() {
    return createdWithDeviceId;
  }

  public StopOrigin getOrigin() {
    return origin;
  }

  public List<String> getNotes() {
    return notes;
  }

  public boolean isLocked() {
    return locked;
  }

  public Truck getTruck() {
    return truck;
  }

  /**
   * Returns the time the stop was last automatically updated (could be null if it was only manually entered).
   */
  public @Nullable DateTime getLastUpdated() {
    return lastUpdated;
  }

  public DateTime getStartTime() {
    return startTime;
  }

  public DateTime getEndTime() {
    return endTime;
  }

  public Location getLocation() {
    return location;
  }

  public Interval timeInterval() {
    return new Interval(startTime, endTime);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(truck, startTime, endTime, location);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o == null) {
      return false;
    } else if (!(o instanceof TruckStop)) {
      return false;
    }
    TruckStop obj = (TruckStop) o;
    return truck.equals(obj.truck) && startTime.equals(obj.startTime) &&
        endTime.equals(obj.endTime) &&
        location.equals(obj.location);
  }

  @Override
  public String toString() {
    try {
      return MoreObjects.toStringHelper(this)
          .add("truck", truck.getId())
          .add("startTime", startTime)
          .add("endTime", endTime)
          .add("lastUpdated", lastUpdated)
          .add("location", location)
          .add("createdWithDevice", createdWithDeviceId)
          .toString();
    } catch (Throwable t) {
      log.log(Level.WARNING, t.getMessage(), t);
      return truck.getId();
    }
  }

  /**
   * Returns a new TruckStop with a new startTime
   */
  public TruckStop withStartTime(DateTime startTime) {
    return TruckStop.builder(this).startTime(startTime).build();
  }

  /**
   * Returns a new TruckStop with a new endTime
   */
  public TruckStop withEndTime(DateTime endTime) {
    return TruckStop.builder(this).endTime(endTime).build();
  }

  public boolean activeDuring(DateTime dateTime) {
    return startTime.equals(dateTime) || (dateTime.isAfter(startTime) && dateTime.isBefore(endTime));
  }

  public boolean isActiveNow() {
    return activeDuring(new DateTime(startTime.getZone()));
  }

  public TruckStop withLocation(Location location) {
    return TruckStop.builder(this).location(location).build();
  }

  /**
   * Returns true if the stop has expired by the specified time.
   */
  public boolean hasExpiredBy(DateTime currentTime) {
    return endTime.isBefore(currentTime);
  }

  public @Nullable DateTime getBeaconTime() {
    return fromBeacon;
  }

  public boolean isFromBeacon() {
    return fromBeacon != null;
  }

  public Interval getInterval() {
    return new Interval(getStartTime(), getEndTime());
  }

  public static class ActiveAfterPredicate implements Predicate<TruckStop> {
    private DateTime requestTime;

    public ActiveAfterPredicate(DateTime requestTime) {
      this.requestTime = requestTime;
    }

    @Override
    public boolean apply(TruckStop input) {
      return input.getStartTime().isAfter(requestTime);
    }
  }

  public static class ActiveDuringPredicate implements Predicate<TruckStop> {
    private DateTime requestTime;

    public ActiveDuringPredicate(DateTime requestTime) {
      this.requestTime = requestTime;
    }

    @Override
    public boolean apply(TruckStop input) {
      return input.activeDuring(requestTime);
    }
  }

  public static class Builder {
    private Truck truck;
    private DateTime startTime;
    private DateTime endTime;
    private Location location;
    private boolean locked;
    private @Nullable DateTime fromBeacon;
    private @Nullable Long key;
    private @Nullable DateTime lastUpdated;
    private List<String> notes = Lists.newLinkedList();
    private StopOrigin origin = StopOrigin.UNKNOWN;
    private @Nullable Long createdWithDeviceId;

    private Builder() {
    }

    private Builder(TruckStop stop) {
      truck = stop.getTruck();
      startTime = stop.getStartTime();
      endTime = stop.getEndTime();
      location = stop.getLocation();
      locked = stop.isLocked();
      fromBeacon = stop.getBeaconTime();
      key = (Long) stop.getKey();
      lastUpdated = stop.getLastUpdated();
      notes = Lists.newLinkedList(stop.getNotes());
      origin = stop.getOrigin();
      createdWithDeviceId = stop.createdWithDeviceId;
    }

    public Builder createdWithDeviceId(Long deviceId) {
      this.createdWithDeviceId = deviceId;
      return this;
    }

    public Builder origin(StopOrigin origin) {
      this.origin = origin;
      return this;
    }

    public Builder notes(List<String> notes) {
      this.notes = Lists.newLinkedList(notes);
      return this;
    }

    public Builder appendNote(String note) {
      this.notes.add(note);
      return this;
    }

    public Builder key(@Nullable Long key) {
      this.key = key;
      return this;
    }

    public Builder truck(Truck truck) {
      this.truck = truck;
      return this;
    }

    public Builder startTime(DateTime dateTime) {
      this.startTime = dateTime;
      return this;
    }

    public Builder lastUpdated(@Nullable DateTime lastUpdated) {
      this.lastUpdated = lastUpdated;
      return this;
    }

    public Builder endTime(DateTime endTime) {
      this.endTime = endTime;
      return this;
    }

    public Builder location(Location location) {
      this.location = location;
      return this;
    }

    public Builder locked(boolean locked) {
      this.locked = locked;
      return this;
    }

    public Builder fromBeacon(@Nullable DateTime fromBeacon) {
      this.fromBeacon = fromBeacon;
      return this;
    }

    public TruckStop build() {
      return new TruckStop(this);
    }

    public Builder prependNotes(List<String> notes) {
      LinkedList<String> newNotes = Lists.newLinkedList(notes);
      newNotes.addAll(this.notes);
      this.notes = newNotes;
      return this;
    }

    public DateTime startTime() {
      return startTime;
    }

    public boolean hasTimes() {
      return startTime != null && endTime != null;
    }

    public DateTime endTime() {
      return endTime;
    }

    public boolean hasCategory(String category) {
      return (truck != null && truck.getCategories().contains(category));
    }
  }
}
