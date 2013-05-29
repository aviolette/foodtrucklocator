package foodtruck.dao.appengine;

import com.google.inject.AbstractModule;

import foodtruck.dao.AddressRuleScriptDAO;
import foodtruck.dao.ApplicationDAO;
import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.EventDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.ScheduleDAO;
import foodtruck.dao.SystemStatDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.TweetCacheDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;

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
    bind(SystemStatDAO.class).to(SystemStatsDAOAppEngine.class);
    bind(AddressRuleScriptDAO.class).to(AddressRuleScriptDAOAppEngine.class);
    bind(TwitterNotificationAccountDAO.class).to(TwitterNotificationAccountDAOAppEngine.class);
    bind(ApplicationDAO.class).to(ApplicationDAOAppEngine.class);
    bind(EventDAO.class).to(EventDAOAppengine.class);
  }
}
