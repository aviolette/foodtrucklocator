package foodtruck.schedule;

/**
 * @author aviolette
 * @since 5/29/13
 */
public interface ScheduleCacher {
  String findSchedule();

  void saveSchedule(String payload);

  void invalidate();
}
