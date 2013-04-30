package foodtruck.server;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import foodtruck.server.api.TweetUpdateServlet;
import foodtruck.server.dashboard.AddressRuleServlet;
import foodtruck.server.dashboard.AdminDashboardServlet;
import foodtruck.server.dashboard.ApplicationServlet;
import foodtruck.server.dashboard.ConfigurationServlet;
import foodtruck.server.dashboard.LocationEditServlet;
import foodtruck.server.dashboard.LocationListServlet;
import foodtruck.server.dashboard.NotificationServlet;
import foodtruck.server.dashboard.StatsServlet;
import foodtruck.server.dashboard.TestNotificationServlet;
import foodtruck.server.dashboard.TruckListServlet;
import foodtruck.server.dashboard.TruckServlet;
import foodtruck.server.dashboard.TruckStopServlet;
import foodtruck.server.job.MailUpdatesServlet;
import foodtruck.server.job.PurgeStatsServlet;
import foodtruck.server.job.RecacheServlet;
import foodtruck.server.job.SendLunchNotificationsServlet;
import foodtruck.server.job.SyncFacebookProfiles;
import foodtruck.server.job.TweetCacheUpdateServlet;
import foodtruck.server.job.TwitterCachePurgeServlet;
import foodtruck.server.job.UpdateTruckStats;

/**
 * Wires all the endpoints for the application.
 * @author aviolette
 * @since Jul 12, 2011
 */
public class FoodtruckServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    // This allows for us to backup the app-engine datastore locally
    bind(com.google.apphosting.utils.remoteapi.RemoteApiServlet.class).in(Singleton.class);
    serve("/remote_api").with(com.google.apphosting.utils.remoteapi.RemoteApiServlet.class);
    serve("/cron/recache").with(RecacheServlet.class);
    serve("/cron/tweets").with(TweetCacheUpdateServlet.class);
    serve("/cron/tweetPurge").with(TwitterCachePurgeServlet.class);
    serve("/cron/processChanges").with(MailUpdatesServlet.class);
    serve("/cron/statPurge").with(PurgeStatsServlet.class);
    serve("/cron/notifications").with(SendLunchNotificationsServlet.class);
    serve("/cron/facebookSync").with(SyncFacebookProfiles.class);
    serve("/cron/updateTruckStats").with(UpdateTruckStats.class);
    serve("/admin").with(AdminDashboardServlet.class);
    serve("/admin/addresses").with(AddressRuleServlet.class);
    serveRegex("/admin/trucks/[\\S]*/events/[\\w]*").with(TruckStopServlet.class);
    serve("/admin/trucks/*").with(TruckServlet.class);
    serve("/admin/trucks").with(TruckListServlet.class);
    serve("/admin/locations/*").with(LocationEditServlet.class);
    serve("/admin/locations").with(LocationListServlet.class);
    serve("/admin/stats").with(StatsServlet.class);
    serve("/admin/notifications").with(NotificationServlet.class);
    serve("/admin/configuration").with(ConfigurationServlet.class);
    serve("/admin/applications").with(ApplicationServlet.class);
    serve("/admin/notificationTest").with(TestNotificationServlet.class);
    serve("/services/*").with(GuiceContainer.class,
        ImmutableMap.of(PackagesResourceConfig.PROPERTY_PACKAGES, "foodtruck.server.resources"));
    serve("/service/tweets").with(TweetUpdateServlet.class);
    serveRegex("/[\\w]*").with(FoodTruckServlet.class);
  }

  @Provides @Named("remote.tweet.update")
  public boolean provideIsTweetUpdateEnabled() {
    return "true".equals(System.getProperty("remote.tweet.update"));
  }
}
