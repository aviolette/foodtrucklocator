package foodtruck.appengine.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.WeeklyLocationStatsRollupDAO;
import foodtruck.model.Slots;
import foodtruck.util.WeeklyRollup;

/**
 * @author aviolette
 * @since 3/9/15
 */
class WeeklyLocationStatsRollupDAOAppEngine extends TimeSeriesDAOAppEngine implements WeeklyLocationStatsRollupDAO {
  @Inject
  public WeeklyLocationStatsRollupDAOAppEngine(Provider<DatastoreService> provider, @WeeklyRollup Slots slotter) {
    super("weekly_location_stat", provider, slotter);
  }
}
