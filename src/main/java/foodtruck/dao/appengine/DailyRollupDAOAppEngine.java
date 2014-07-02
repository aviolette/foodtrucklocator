package foodtruck.dao.appengine;

import com.google.inject.Inject;

import foodtruck.dao.DailyRollupDAO;
import foodtruck.util.DailyRollup;
import foodtruck.util.Slots;

/**
 * @author aviolette
 * @since 7/2/14
 */
public class DailyRollupDAOAppEngine extends TimeSeriesDAOAppEngine implements DailyRollupDAO {

  @Inject
  public DailyRollupDAOAppEngine(DatastoreServiceProvider provider, @DailyRollup Slots slotter) {
    super("daily_stat", provider, slotter);
  }
}
