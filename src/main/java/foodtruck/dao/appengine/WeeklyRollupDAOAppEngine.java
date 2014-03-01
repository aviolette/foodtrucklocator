package foodtruck.dao.appengine;

import com.google.inject.Inject;

import foodtruck.dao.WeeklyRollupDAO;
import foodtruck.util.Slots;
import foodtruck.util.WeeklyRollup;

/**
 * @author aviolette
 * @since 2/28/14
 */
public class WeeklyRollupDAOAppEngine extends TimeSeriesDAOAppEngine
    implements WeeklyRollupDAO {

  @Inject
  public WeeklyRollupDAOAppEngine(DatastoreServiceProvider provider, @WeeklyRollup Slots slotter) {
    super("weekly_stat", provider, slotter);
  }
}