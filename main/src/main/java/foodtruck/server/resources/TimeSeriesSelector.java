package foodtruck.server.resources;

import com.google.inject.Inject;

import foodtruck.dao.DailyRollupDAO;
import foodtruck.dao.DailyTruckStopDAO;
import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.dao.TimeSeriesDAO;
import foodtruck.dao.WeeklyLocationStatsRollupDAO;
import foodtruck.dao.WeeklyRollupDAO;
import foodtruck.dao.WeeklyTruckStopDAO;
import foodtruck.server.job.PurgeStatsServlet;

/**
 * @author aviolette
 * @since 9/21/16
 */
class TimeSeriesSelector {
  private static final long DAY_IN_MILLIS = 86400000L;
  private final WeeklyTruckStopDAO weeklyTruckStopDAO;
  private final WeeklyRollupDAO weeklyRollupDAO;
  private final WeeklyLocationStatsRollupDAO weeklyLocationDAO;
  private final DailyRollupDAO dailyRollupDAO;
  private final DailyTruckStopDAO dailyTruckStopDAO;
  private final FifteenMinuteRollupDAO fifteenMinuteRollupDAO;

  @Inject
  public TimeSeriesSelector(WeeklyTruckStopDAO weeklyTruckStopDAO, WeeklyRollupDAO weeklyRollupDAO,
      WeeklyLocationStatsRollupDAO weeklyLocationDAO, DailyRollupDAO dailyRollupDAO,
      DailyTruckStopDAO dailyTruckStopDAO, FifteenMinuteRollupDAO fifteenMinuteRollupDAO) {
    this.weeklyTruckStopDAO = weeklyTruckStopDAO;
    this.weeklyRollupDAO = weeklyRollupDAO;
    this.weeklyLocationDAO = weeklyLocationDAO;
    this.dailyRollupDAO = dailyRollupDAO;
    this.dailyTruckStopDAO = dailyTruckStopDAO;
    this.fifteenMinuteRollupDAO = fifteenMinuteRollupDAO;
  }

  public TimeSeriesDAO select(long interval, String statName) {
    TimeSeriesDAO weekly = weeklyRollupDAO, daily = dailyRollupDAO;
    if (statName.contains("location")) {
      weekly = weeklyLocationDAO;
    } else if (statName.equals(PurgeStatsServlet.TRUCK_STOPS) || statName.equals(PurgeStatsServlet.UNIQUE_TRUCKS)) {
      weekly = weeklyTruckStopDAO;
      daily = dailyTruckStopDAO;
    }
    // TODO: this is a really stupid way to do it
    if (interval == 604800000L) {
      return weekly;
    } else if (interval == DAY_IN_MILLIS) {
      return daily;
    } else {
      return fifteenMinuteRollupDAO;
    }
  }
}
