package foodtruck.appengine.dao.appengine;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import foodtruck.dao.AddressRuleScriptDAO;
import foodtruck.dao.AlexaExchangeDAO;
import foodtruck.dao.ApplicationDAO;
import foodtruck.dao.DailyDataDAO;
import foodtruck.dao.LinxupAccountDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.MenuDAO;
import foodtruck.dao.MessageDAO;
import foodtruck.dao.NotificationDeviceProfileDAO;
import foodtruck.dao.RetweetsDAO;
import foodtruck.dao.ReverseLookupDAO;
import foodtruck.dao.SlackWebhookDAO;
import foodtruck.dao.StoryDAO;
import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckObserverDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.dao.UserDAO;
import foodtruck.model.Slots;
import foodtruck.util.Secondary;

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
    bind(UserDAO.class).to(UserDAOAppEngine.class);
    bind(ReverseLookupDAO.class).to(ReverseLookupDAOAppEngine.class);
    bind(SlackWebhookDAO.class).to(SlackWebhookDAOAppEngine.class);
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
