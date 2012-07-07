// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.model;

/**
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
public class TimeValue {
  private final long timestamp;
  private final long count;

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
}
