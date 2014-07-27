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
import foodtruck.server.dashboard.EventListServlet;
import foodtruck.server.dashboard.EventServlet;
import foodtruck.server.dashboard.LocationEditServlet;
import foodtruck.server.dashboard.LocationListServlet;
import foodtruck.server.dashboard.MessageEditServlet;
import foodtruck.server.dashboard.MessageListServlet;
import foodtruck.server.dashboard.NotificationServlet;
import foodtruck.server.dashboard.ObserverServlet;
import foodtruck.server.dashboard.PromoteServlet;
import foodtruck.server.dashboard.StatsServlet;
import foodtruck.server.dashboard.SyncServlet;
import foodtruck.server.dashboard.TestNotificationServlet;
import foodtruck.server.dashboard.TruckListServlet;
import foodtruck.server.dashboard.TruckRequestServlet;
import foodtruck.server.dashboard.TruckServlet;
import foodtruck.server.dashboard.TruckStopServlet;
import foodtruck.server.delivery.RequestATruckLandingServlet;
import foodtruck.server.delivery.RequestATruckServlet;
import foodtruck.server.delivery.ViewRequestATruckServlet;
import foodtruck.server.job.BuildHeatmapServlet;
import foodtruck.server.job.CreateError;
import foodtruck.server.job.ErrorCountServlet;
import foodtruck.server.job.InvalidateScheduleCache;
import foodtruck.server.job.MigrateTimeSeries;
import foodtruck.server.job.PurgeStatsServlet;
import foodtruck.server.job.RecacheServlet;
import foodtruck.server.job.SendLunchNotificationsServlet;
import foodtruck.server.job.SyncFacebookProfiles;
import foodtruck.server.job.TweetCacheUpdateServlet;
import foodtruck.server.job.TwitterCachePurgeServlet;
import foodtruck.server.job.UpdateTruckStats;
import foodtruck.server.migrations.ForceSaveTruck;
import foodtruck.server.petitions.PetitionEmailInterstitialServlet;
import foodtruck.server.petitions.PetitionServlet;
import foodtruck.server.petitions.PetitionThanksServlet;
import foodtruck.server.petitions.PetitionVerificationServlet;

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
    serve("/cron/create_error").with(CreateError.class);
    serve("/cron/recache").with(RecacheServlet.class);
    serve("/cron/tweets").with(TweetCacheUpdateServlet.class);
    serve("/cron/tweetPurge").with(TwitterCachePurgeServlet.class);
    serve("/cron/statPurge").with(PurgeStatsServlet.class);
    serve("/cron/notifications").with(SendLunchNotificationsServlet.class);
    serve("/cron/facebookSync").with(SyncFacebookProfiles.class);
    serve("/cron/build-heatmap").with(BuildHeatmapServlet.class);
    serve("/cron/updateTruckStats").with(UpdateTruckStats.class);
    serve("/cron/forcesavetruck").with(ForceSaveTruck.class);
    serve("/cron/migrateWeeklyStats").with(MigrateTimeSeries.class);
    serve("/cron/invalidateCache").with(InvalidateScheduleCache.class);
    serve("/cron/error_stats").with(ErrorCountServlet.class);
    serve("/admin").with(AdminDashboardServlet.class);
    serve("/admin/addresses").with(AddressRuleServlet.class);
    serveRegex("/admin/trucks/[\\S]*/events/[\\w]*").with(TruckStopServlet.class);
    serve("/admin/trucks/*").with(TruckServlet.class);
    serve("/admin/trucks").with(TruckListServlet.class);
    serve("/admin/locations/*").with(LocationEditServlet.class);
    serve("/admin/locations", "/admin/locations;*").with(LocationListServlet.class);
    serve("/admin/messages/*").with(MessageEditServlet.class);
    serve("/admin/messages").with(MessageListServlet.class);
    serve("/admin/stats").with(StatsServlet.class);
    serve("/admin/sync").with(SyncServlet.class);
    serve("/admin/notifications").with(NotificationServlet.class);
    serve("/admin/configuration").with(ConfigurationServlet.class);
    serve("/admin/applications").with(ApplicationServlet.class);
    serve("/admin/lookouts").with(ObserverServlet.class);
    serve("/admin/notificationTest").with(TestNotificationServlet.class);
    serve("/admin/events").with(EventListServlet.class);
    serve("/admin/events/*").with(EventServlet.class);
    serve("/admin/requests/edit/*").with(TruckRequestServlet.class);
    serve("/admin/requests/promote").with(PromoteServlet.class);
    serve("/vendor").with(VendorServlet.class);
    serve("/vendor/recache/*").with(VendorRecacheServlet.class);
    serve("/vendor/offtheroad/*").with(VendorOffTheRoadServlet.class);
    serve("/vendor/settings/*").with(VendorSettingsServlet.class);
    serve("/services/*").with(GuiceContainer.class,
        ImmutableMap.of(PackagesResourceConfig.PROPERTY_PACKAGES, "foodtruck.server.resources"));
    serve("/service/tweets").with(TweetUpdateServlet.class);
    serve("/weekly-schedule").with(WeeklyScheduleServlet.class);
    serve("/vendor/beaconnaise").with(BeaconnaiseServlet.class);
    serve("/trucks*").with(TrucksServlet.class);
    serve("/request").with(RequestATruckLandingServlet.class);
    serve("/requests/edit/*").with(RequestATruckServlet.class);
    serve("/requests/view/*").with(ViewRequestATruckServlet.class);
    serve("/locations*").with(LocationServlet.class);
    serve("/events*").with(EventsServlet.class);
    serve("/petitions/600w").with(PetitionServlet.class);
    serve("/petitions/600w/not_finished").with(PetitionEmailInterstitialServlet.class);
    serve("/petitions/600w/verify/*").with(PetitionVerificationServlet.class);
    serve("/petitions/600w/thanks").with(PetitionThanksServlet.class);
    serve("/stats/heatmap").with(HeatmapServlet.class);
    serve("/stats/timeline").with(TruckTimelineServlet.class);
    serveRegex("/[\\w]*").with(FoodTruckServlet.class);
  }

  @Provides @Named("remote.tweet.update")
  public boolean provideIsTweetUpdateEnabled() {
    return "true".equals(System.getProperty("remote.tweet.update"));
  }
}
