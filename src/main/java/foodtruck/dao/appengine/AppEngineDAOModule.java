package foodtruck.dao.appengine;

import com.google.inject.AbstractModule;

import foodtruck.dao.AddressRuleDAO;
import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.ScheduleDAO;
import foodtruck.dao.SystemStatDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopChangeDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.TweetCacheDAO;

/**
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
public class AppEngineDAOModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TruckStopDAO.class).to(TruckStopDAOAppEngine.class);
    bind(LocationDAO.class).to(LocationDAOAppEngine.class);
    bind(TweetCacheDAO.class).to(TweetCacheAppEngineDAO.class);
    bind(ScheduleDAO.class).to(ScheduleDAOAppEngine.class);
    bind(TruckDAO.class).to(TruckDAOAppEngine.class);
    bind(ConfigurationDAO.class).to(ConfigurationDAOAppEngine.class);
    bind(TruckStopChangeDAO.class).to(TruckStopChangeDAOAppEngine.class);
    bind(SystemStatDAO.class).to(SystemStatsDAOAppEngine.class);
    bind(AddressRuleDAO.class).to(AddressRuleDAOAppEngine.class);
  }
}
