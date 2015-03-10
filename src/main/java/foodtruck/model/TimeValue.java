package foodtruck.model;

import java.io.Serializable;

import com.google.common.base.Objects;

/**
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
public class TimeValue implements Serializable {
  private final long timestamp;
  private long count;

  public TimeValue(long timestamp, long count) {
    this.timestamp = timestamp;
    this.count = count;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("timeStamp", timestamp)
        .add("count", count).toString();
  }
}
