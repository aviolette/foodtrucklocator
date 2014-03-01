package foodtruck.dao;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import foodtruck.model.SystemStats;

/**
 * @author aviolette
 * @since 2/27/14
 */
public interface TimeSeriesDAO extends DAO<Long, SystemStats>{
  List<SystemStats> findWithinRange(long startTime, long endTime);

  /**
   * Updates a count on a stat
   * @param timestamp the time the count occurred
   * @param key the property name
   */
  void updateCount(DateTime timestamp, String key);


  /**
   * Deletes all stats before the current day
   * @param localDate
   */
  void deleteBefore(LocalDate localDate);

  void updateCount(DateTime timestamp, String statName, long by);

  void updateCount(long timestamp, String statName, long by);
}
