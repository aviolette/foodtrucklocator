package foodtruck.dao.appengine;

import com.google.inject.AbstractModule;

import foodtruck.dao.AddressRuleScriptDAO;
import foodtruck.dao.ApplicationDAO;
import foodtruck.dao.DailyDataDAO;
import foodtruck.dao.DailyRollupDAO;
import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.MessageDAO;
import foodtruck.dao.NotificationDeviceProfileDAO;
import foodtruck.dao.RetweetsDAO;
import foodtruck.dao.StoryDAO;
import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckObserverDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.dao.WeeklyLocationStatsRollupDAO;
import foodtruck.dao.WeeklyRollupDAO;
import foodtruck.util.Secondary;

/**
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
public class AppEngineDAOModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(AddressRuleScriptDAO.class).to(AddressRuleScriptDAOAppEngine.class);
    bind(ApplicationDAO.class).to(ApplicationDAOAppEngine.class);
    bind(DailyDataDAO.class).to(DailyDataDAOAppEngine.class);
    bind(DailyRollupDAO.class).to(DailyRollupDAOAppEngine.class);
    bind(FifteenMinuteRollupDAO.class).to(FifteenMinuteRollupDAOAppEngine.class);
    bind(LocationDAO.class).to(LocationDAOAppEngine.class);
    bind(MessageDAO.class).to(MessageDAOAppEngine.class);
    bind(NotificationDeviceProfileDAO.class).to(NotificationDeviceProfileDAOAppEngine.class);
    bind(RetweetsDAO.class).to(RetweetDAOAppEngine.class);
    bind(StoryDAO.class).to(StoryDAOAppEngine.class);
    bind(TrackingDeviceDAO.class).to(TrackingDeviceDAOAppEngine.class);
    bind(TruckDAO.class).annotatedWith(Secondary.class).to(TruckDAOAppEngine.class);
    bind(TruckObserverDAO.class).to(TruckObserverDAOAppEngine.class);
    bind(TruckStopDAO.class).to(TruckStopDAOAppEngine.class);
    bind(TwitterNotificationAccountDAO.class).to(TwitterNotificationAccountDAOAppEngine.class);
    bind(WeeklyLocationStatsRollupDAO.class).to(WeeklyLocationStatsRollupDAOAppEngine.class);
    bind(WeeklyRollupDAO.class).to(WeeklyRollupDAOAppEngine.class);
  }
}
