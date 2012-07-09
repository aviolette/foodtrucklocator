package foodtruck.stats;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

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
    Iterator<SystemStats> statsIterator = stats.iterator();
    long currentValue = -1;
    SystemStats stat = null;
    for (long i = startSlot; i < endTime; i = i + FIFTEEN_MIN_IN_MS) {
      if (startSlot > currentValue && statsIterator.hasNext()) {
        stat = statsIterator.next();
        currentValue = stat.getTimeStamp();
      }
      if (i == currentValue) {
        dataPoints.add(new TimeValue(i, stat.getStat(statName)));
        if (statsIterator.hasNext()) {
          stat = statsIterator.next();
          currentValue = stat.getTimeStamp();
        }
      } else {
        dataPoints.add(new TimeValue(i, 0));
      }

    }
    return new StatVector(statName, dataPoints.build());
  }

  public static long getSlot(long time) {
    return (long) Math.floor((double) time / (double) FIFTEEN_MIN_IN_MS) * FIFTEEN_MIN_IN_MS;
  }
}
