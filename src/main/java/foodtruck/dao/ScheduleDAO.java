// Copyright 2010 BrightTag, Inc. All rights reserved.
package foodtruck.dao;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;

/**
 * A DAO for caching and retrieving the daily schedule in JSON form.
 * @author aviolette@gmail.com
 * @since 12/7/11
 */
public interface ScheduleDAO {
  /**
   * Returns the schedule for the current day, or {@code null} if it cannot be found
   * @param day the day
   * @return the schedule or {@code null}
   */
  @Nullable String findSchedule(LocalDate day);
}
