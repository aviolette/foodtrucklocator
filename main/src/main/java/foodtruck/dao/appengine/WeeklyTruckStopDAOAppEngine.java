package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.WeeklyTruckStopDAO;
import foodtruck.model.Slots;
import foodtruck.util.WeeklyRollup;

/**
 * @author aviolette
 * @since 9/21/16
 */
public class WeeklyTruckStopDAOAppEngine extends TimeSeriesDAOAppEngine implements WeeklyTruckStopDAO {
  @Inject
  public WeeklyTruckStopDAOAppEngine(Provider<DatastoreService> provider, @WeeklyRollup Slots slotter) {
    super("weekly_truck_stop_stats", provider, slotter);
  }
}
