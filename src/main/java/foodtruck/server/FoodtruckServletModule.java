package foodtruck.server;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import org.joda.time.DateTimeZone;

import foodtruck.server.api.DailyScheduleServlet;
import foodtruck.server.api.TweetUpdateServlet;
import foodtruck.server.dashboard.AddressRuleServlet;
import foodtruck.server.dashboard.AdminDashboardServlet;
import foodtruck.server.dashboard.ConfigurationServlet;
import foodtruck.server.dashboard.LocationEditServlet;
import foodtruck.server.dashboard.LocationListServlet;
import foodtruck.server.dashboard.StatsServlet;
import foodtruck.server.dashboard.TruckListServlet;
import foodtruck.server.dashboard.TruckServlet;
import foodtruck.server.job.MailUpdatesServlet;
import foodtruck.server.job.RecacheServlet;
import foodtruck.server.job.TweetCacheUpdateServlet;
import foodtruck.server.job.TwitterCachePurgeServlet;

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
    serve("/admin").with(AdminDashboardServlet.class);
    serve("/admin/addresses").with(AddressRuleServlet.class);
    serve("/admin/trucks/*").with(TruckServlet.class);
    serve("/admin/trucks").with(TruckListServlet.class);
    serve("/admin/locations/*").with(LocationEditServlet.class);
    serve("/admin/locations").with(LocationListServlet.class);
    serve("/admin/stats").with(StatsServlet.class);
    serve("/admin/configuration").with(ConfigurationServlet.class);
    serve("/service/schedule").with(DailyScheduleServlet.class);
    serve("/services/*").with(GuiceContainer.class,
        ImmutableMap.of(PackagesResourceConfig.PROPERTY_PACKAGES, "foodtruck.server.resources"));
    serve("/service/tweets").with(TweetUpdateServlet.class);
    serve("/trucks*").with(TruckInfoServlet.class);
    serveRegex("/[\\w]*").with(FoodTruckServlet.class);
  }

  @Provides
  public DateTimeZone provideDefaultZone() {
    return DateTimeZone.forID("America/Chicago");
  }

  @Provides @Named("remote.tweet.update")
  public boolean provideIsTweetUpdateEnabled() {
    return "true".equals(System.getProperty("remote.tweet.update"));
  }
}
