package foodtruck.model;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * @author aviolette@gmail.com
 * @since 7/7/12
 */
public class Slots {
  private final long slotLength;

  public Slots(long slotLength) {
    this.slotLength = slotLength;
  }

  public StatVector fillIn(List<SystemStats> stats, String statName, long startTime,
      long endTime) {
    ImmutableList.Builder<TimeValue> dataPoints = ImmutableList.builder();
    long startSlot = getSlot(startTime);
    Map<Long, TimeValue> timeValues = Maps.newHashMap();
    for (long i = startSlot; i < endTime; i = i + slotLength) {
      final TimeValue timeValue = new TimeValue(i, 0);
      // TODO: use a map that maintains insertion order
      dataPoints.add(timeValue);
      timeValues.put(timeValue.getTimestamp(), timeValue);
    }
    for (SystemStats stat : stats) {
      long slot = getSlot(stat.getTimeStamp());
      TimeValue value = timeValues.get(slot);
      value.setCount(stat.getStat(statName));
    }
    return new StatVector(statName, dataPoints.build());
  }

  public long getSlot(long time) {
    return (long) Math.floor((double) time / (double) slotLength) * slotLength;
  }
}
