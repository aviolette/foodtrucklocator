package foodtruck.dao;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;

import foodtruck.model.DailyData;

/**
 * @author aviolette
 * @since 10/26/15
 */
public interface DailyDataDAO extends DAO<Long, DailyData> {
  @Nullable
  DailyData findByLocationAndDay(String locationName, LocalDate date);
}
