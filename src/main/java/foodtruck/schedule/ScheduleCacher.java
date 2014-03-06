package foodtruck.schedule;

import javax.annotation.Nullable;

/**
 * @author aviolette
 * @since 5/29/13
 */
public interface ScheduleCacher {
  @Nullable String findSchedule();

  void saveSchedule(String payload);

  void invalidate();
}
