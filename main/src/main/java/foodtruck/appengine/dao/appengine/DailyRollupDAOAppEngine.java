package foodtruck.appengine.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.DailyRollupDAO;
import foodtruck.model.Slots;

/**
 * @author aviolette
 * @since 7/2/14
 */
class DailyRollupDAOAppEngine extends TimeSeriesDAOAppEngine implements DailyRollupDAO {

  @Inject
  public DailyRollupDAOAppEngine(Provider<DatastoreService> provider, @DailyRollup Slots slotter) {
    super("daily_stat", provider, slotter);
  }
}
