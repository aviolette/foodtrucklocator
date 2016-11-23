package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.DailyTruckStopDAO;
import foodtruck.util.DailyRollup;
import foodtruck.util.Slots;

/**
 * @author aviolette
 * @since 9/21/16
 */
public class DailyTruckStopDAOAppEngine extends TimeSeriesDAOAppEngine implements DailyTruckStopDAO {
  @Inject
  public DailyTruckStopDAOAppEngine(Provider<DatastoreService> provider, @DailyRollup Slots slotter) {
    super("daily_truck_stop_stats", provider, slotter);
  }
}
