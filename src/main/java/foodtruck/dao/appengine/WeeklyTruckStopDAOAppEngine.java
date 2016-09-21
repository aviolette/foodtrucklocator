package foodtruck.dao.appengine;

import com.google.inject.Inject;

import foodtruck.dao.WeeklyTruckStopDAO;
import foodtruck.util.DailyRollup;
import foodtruck.util.Slots;

/**
 * @author aviolette
 * @since 9/21/16
 */
public class WeeklyTruckStopDAOAppEngine extends TimeSeriesDAOAppEngine implements WeeklyTruckStopDAO {
  @Inject
  public WeeklyTruckStopDAOAppEngine(DatastoreServiceProvider provider, @DailyRollup Slots slotter) {
    super("weekly_truck_stop_stats", provider, slotter);
  }
}
