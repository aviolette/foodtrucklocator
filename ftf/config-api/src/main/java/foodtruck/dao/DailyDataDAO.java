package foodtruck.dao;

import java.util.List;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;

import foodtruck.model.DailyData;

/**
 * @author aviolette
 * @since 10/26/15
 */
public interface DailyDataDAO extends DAO<Long, DailyData> {
  /**
   * Finds the DailyData by location name.  Returns null if it cannot be found
   */
  @Nullable DailyData findByLocationAndDay(String locationName, LocalDate date);

  /**
   * Finds the DailyData by truck ID.  Returns null if it cannot be found
   */
  @Nullable DailyData findByTruckAndDay(String truckId, LocalDate date);

  /**
   * Finds all the truck specials for a specific day
   */
  List<DailyData> findTruckSpecialsByDay(LocalDate day);
}
