package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.WeeklyRollupDAO;
import foodtruck.model.Slots;
import foodtruck.util.WeeklyRollup;

/**
 * @author aviolette
 * @since 2/28/14
 */
class WeeklyRollupDAOAppEngine extends TimeSeriesDAOAppEngine implements WeeklyRollupDAO {

  @Inject
  public WeeklyRollupDAOAppEngine(Provider<DatastoreService> provider, @WeeklyRollup Slots slotter) {
    super("weekly_stat", provider, slotter);
  }
}