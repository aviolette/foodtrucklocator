package foodtruck.dao;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import foodtruck.model.SystemStats;

/**
 * @author aviolette@gmail.com
 * @since 7/5/12
 */
public interface SystemStatDAO extends DAO<Long, SystemStats> {
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
}
