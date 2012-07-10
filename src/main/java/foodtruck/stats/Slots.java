package foodtruck.stats;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import foodtruck.model.StatVector;
import foodtruck.model.SystemStats;
import foodtruck.model.TimeValue;

/**
 * @author aviolette@gmail.com
 * @since 7/7/12
 */
public class Slots {
  private final static long FIFTEEN_MIN_IN_MS = 900000;

  public static StatVector fillIn(List<SystemStats> stats, String statName, long startTime,
      long endTime) {
    ImmutableList.Builder<TimeValue> dataPoints = ImmutableList.builder();
    long startSlot = getSlot(startTime);
    Map<Long, TimeValue> timeValues = Maps.newHashMap();
    for (long i = startSlot; i < endTime; i = i + FIFTEEN_MIN_IN_MS) {
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

  public static long getSlot(long time) {
    return (long) Math.floor((double) time / (double) FIFTEEN_MIN_IN_MS) * FIFTEEN_MIN_IN_MS;
  }
}
