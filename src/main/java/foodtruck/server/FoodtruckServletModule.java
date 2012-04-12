package foodtruck.server;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.servlet.ServletModule;

import org.joda.time.DateTimeZone;

import foodtruck.model.Location;
import foodtruck.server.api.AdminTruckStopServlet;
import foodtruck.server.api.DailyScheduleServlet;
import foodtruck.server.api.FoodTruckScheduleServlet;
import foodtruck.server.api.TruckStopServlet;
import foodtruck.server.api.TweetUpdateServlet;
import foodtruck.server.dashboard.AdminDashboardServlet;
import foodtruck.server.dashboard.ConfigurationServlet;
import foodtruck.server.dashboard.LocationEditServlet;
import foodtruck.server.dashboard.LocationListServlet;
import foodtruck.server.dashboard.TruckListServlet;
import foodtruck.server.dashboard.TruckServlet;
import foodtruck.server.job.RecacheServlet;
import foodtruck.server.job.SyncTrucksServlet;
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
    if ("true".equals(System.getProperty("enable.remote_api"))) {
      bind(com.google.apphosting.utils.remoteapi.RemoteApiServlet.class).in(Singleton.class);
      serve("/remote_api").with(com.google.apphosting.utils.remoteapi.RemoteApiServlet.class);
    }
    serve("/cron/recache").with(RecacheServlet.class);
    serve("/cron/tweets").with(TweetCacheUpdateServlet.class);
    serve("/cron/synctrucks").with(SyncTrucksServlet.class);
    serve("/cron/tweetPurge").with(TwitterCachePurgeServlet.class);
    serve("/admin").with(AdminDashboardServlet.class);
    serve("/admin/trucks/*").with(TruckServlet.class);
    serve("/admin/trucks").with(TruckListServlet.class);
    serve("/admin/locations/*").with(LocationEditServlet.class);
    serve("/admin/locations").with(LocationListServlet.class);
    serve("/admin/configuration").with(ConfigurationServlet.class);
    serve("/admin/service/stop/*").with(AdminTruckStopServlet.class);
    serve("/service/schedule/*").with(FoodTruckScheduleServlet.class);
    serve("/service/schedule").with(DailyScheduleServlet.class);
    serve("/service/stops*").with(TruckStopServlet.class);
    serve("/service/tweets").with(TweetUpdateServlet.class);
    serve("/heatmap").with(HeatmapServlet.class);
    serveRegex("/[\\w]*").with(FoodTruckServlet.class);
  }

  @Provides
  public DateTimeZone provideDefaultZone() {
    return DateTimeZone.forID("America/Chicago");
  }

  @Provides @Named("center")
  public Location provideMapCenter() {
    return Location.builder().lat(41.8807438).lng(-87.6293867).build();
  }

  @Provides @Named("remote.tweet.update")
  public boolean provideIsTweetUpdateEnabled() {
    return "true".equals(System.getProperty("remote.tweet.update"));
  }
}
