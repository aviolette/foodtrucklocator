package foodtruck.dao.appengine;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import foodtruck.dao.AddressRuleScriptDAO;
import foodtruck.dao.AlexaExchangeDAO;
import foodtruck.dao.ApplicationDAO;
import foodtruck.dao.DailyDataDAO;
import foodtruck.dao.DailyRollupDAO;
import foodtruck.dao.DailyTruckStopDAO;
import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.dao.LinxupAccountDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.MenuDAO;
import foodtruck.dao.MessageDAO;
import foodtruck.dao.NotificationDeviceProfileDAO;
import foodtruck.dao.RetweetsDAO;
import foodtruck.dao.StoryDAO;
import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckObserverDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.dao.UserDAO;
import foodtruck.dao.WeeklyLocationStatsRollupDAO;
import foodtruck.dao.WeeklyRollupDAO;
import foodtruck.dao.WeeklyTruckStopDAO;
import foodtruck.model.Slots;
import foodtruck.util.DailyRollup;
import foodtruck.util.FifteenMinuteRollup;
import foodtruck.util.Secondary;
import foodtruck.util.WeeklyRollup;

/**
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
public class AppEngineDAOModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(AddressRuleScriptDAO.class).to(AddressRuleScriptDAOAppEngine.class);
    bind(AlexaExchangeDAO.class).to(AlexaExchangeDAOAppEngine.class);
    bind(ApplicationDAO.class).to(ApplicationDAOAppEngine.class);
    bind(DailyDataDAO.class).to(DailyDataDAOAppEngine.class);
    bind(DailyRollupDAO.class).to(DailyRollupDAOAppEngine.class);
    bind(FifteenMinuteRollupDAO.class).to(FifteenMinuteRollupDAOAppEngine.class);
    bind(LinxupAccountDAO.class).to(LinxupDAOAppengine.class);
    bind(LocationDAO.class).to(LocationDAOAppEngine.class);
    bind(MessageDAO.class).to(MessageDAOAppEngine.class);
    bind(MenuDAO.class).to(MenuDAOAppEngine.class);
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
    bind(UserDAO.class).to(UserDAOAppEngine.class);
    bind(DailyTruckStopDAO.class).to(DailyTruckStopDAOAppEngine.class);
    bind(WeeklyTruckStopDAO.class).to(WeeklyTruckStopDAOAppEngine.class);
  }

  @DailyRollup
  @Provides
  public Slots provideDailyRollup() {
    return new Slots(1000 * 60 * 60 * 24);
  }

  @FifteenMinuteRollup
  @Provides
  public Slots provideFifteenMinuteRollup() {
    return new Slots(1000 * 60 * 15);
  }


  @WeeklyRollup
  @Provides
  public Slots provideWeeklyRollup() {
    return new Slots(1000 * 60 * 60 * 24 * 7);
  }
}
