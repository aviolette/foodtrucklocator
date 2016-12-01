package foodtruck.truckstops;

import javax.annotation.Nullable;

/**
 * @author aviolette
 * @since 5/29/13
 */
public interface ScheduleCacher {
  @Nullable String findSchedule();

  void saveSchedule(String payload);

  void invalidate();

  @Nullable String findTomorrowsSchedule();

  void saveTomorrowsSchedule(String payload);

}