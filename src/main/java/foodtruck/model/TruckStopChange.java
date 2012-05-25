// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.model;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

/**
 * @author aviolette@gmail.com
 * @since 5/24/12
 */
public class TruckStopChange extends ModelEntity{
  private @Nullable TruckStop to;
  private @Nullable TruckStop from;
  private DateTime timeStamp;

  private TruckStopChange(Builder builder) {
    super(builder.id);
    to = builder.to;
    from = builder.from;
    timeStamp = builder.dateTime;
  }

  public @Nullable TruckStop getTo() {
    return to;
  }

  public @Nullable TruckStop getFrom() {
    return from;
  }

  public DateTime getTimeStamp() {
    return timeStamp;
  }


  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private TruckStop to;
    private TruckStop from;
    private DateTime dateTime;
    private long id = -1;

    private Builder() {}

    public Builder id(long id) {
      this.id = id;
      return this;
    }

    public Builder to(@Nullable TruckStop to) {
      this.to  = to;
      return this;
    }

    public Builder from(@Nullable TruckStop from) {
      this.from = from;
      return this;
    }

    public Builder timeStamp(DateTime dateTime) {
      this.dateTime = dateTime;
      return this;
    }

    public TruckStopChange build() {
      return new TruckStopChange(this);
    }
  }
}
