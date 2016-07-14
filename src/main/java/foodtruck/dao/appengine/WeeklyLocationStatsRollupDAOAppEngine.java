package foodtruck.dao.appengine;

import com.google.inject.Inject;

import foodtruck.dao.WeeklyLocationStatsRollupDAO;
import foodtruck.util.Slots;
import foodtruck.util.WeeklyRollup;

/**
 * @author aviolette
 * @since 3/9/15
 */
class WeeklyLocationStatsRollupDAOAppEngine extends TimeSeriesDAOAppEngine
    implements WeeklyLocationStatsRollupDAO {
  @Inject
  public WeeklyLocationStatsRollupDAOAppEngine(DatastoreServiceProvider provider, @WeeklyRollup Slots slotter) {
    super("weekly_location_stat", provider, slotter);
  }
}
