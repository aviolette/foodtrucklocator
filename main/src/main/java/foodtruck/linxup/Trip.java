package foodtruck.linxup;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.joda.time.DateTime;

import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 11/1/16
 */
public class Trip {
  private Location start;
  private Location end;
  private DateTime startTime;
  private DateTime endTime;
  private List<Position> positions;

  private Trip(Builder builder) {
    this.start = builder.start;
    this.end = builder.end;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.positions = ImmutableList.copyOf(builder.positions);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Trip instance) {
    return new Builder(instance);
  }

  public String getName() {
    return start.getShortenedName() + " to " + end.getShortenedName();
  }

  public Location getStart() {
    return start;
  }

  public Location getEnd() {
    return end;
  }

  public DateTime getStartTime() {
    return startTime;
  }

  public DateTime getEndTime() {
    return endTime;
  }

  public List<Position> getPositions() {
    return positions;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
//        .add("start", start)
//        .add("end", end)
        .add("startTime", startTime)
        .add("endTime", endTime)
        .toString();
  }

  public static class Builder {
    private Location start;
    private Location end;
    private DateTime startTime;
    private DateTime endTime;
    private List<Position> positions = Lists.newLinkedList();

    public Builder() {
    }

    public Builder(Trip instance) {
      this.start = instance.start;
      this.end = instance.end;
      this.startTime = instance.startTime;
      this.endTime = instance.endTime;
      this.positions = instance.positions;
    }

    public Builder start(Location start) {
      this.start = start;
      return this;
    }

    public Builder end(Location end) {
      this.end = end;
      return this;
    }

    public Builder startTime(DateTime startTime) {
      this.startTime = startTime;
      return this;
    }

    public Builder endTime(DateTime endTime) {
      this.endTime = endTime;
      return this;
    }

    public Trip build() {
      return new Trip(this);
    }

    public DateTime getStartTime() {
      return startTime;
    }

    public DateTime getEndTime() {
      return endTime;
    }

    public Builder addPosition(Position position) {
      positions.add(position);
      return this;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("start", start.getShortenedName())
          .add("end", end.getShortenedName())
//          .add("start", start)
//          .add("end", end)
          .add("startTime", startTime)
          .add("endTime", endTime)
          .toString();
    }
  }
}
