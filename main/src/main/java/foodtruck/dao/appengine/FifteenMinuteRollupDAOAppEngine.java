package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.util.FifteenMinuteRollup;
import foodtruck.util.Slots;

/**
 * @author aviolette@gmail.com
 * @since 7/5/12
 */
class FifteenMinuteRollupDAOAppEngine extends TimeSeriesDAOAppEngine implements FifteenMinuteRollupDAO {

  @Inject
  public FifteenMinuteRollupDAOAppEngine(Provider<DatastoreService> provider, @FifteenMinuteRollup Slots slotter) {
    super("fifteen_min_stat", provider, slotter);
  }
}
