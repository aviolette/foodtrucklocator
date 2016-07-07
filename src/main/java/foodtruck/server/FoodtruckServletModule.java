package foodtruck.server;

import com.google.appengine.api.utils.SystemProperty;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import foodtruck.server.dashboard.AddressRuleServlet;
import foodtruck.server.dashboard.AdminDashboardServlet;
import foodtruck.server.dashboard.ApplicationDetailServlet;
import foodtruck.server.dashboard.ApplicationServlet;
import foodtruck.server.dashboard.CompoundEventServlet;
import foodtruck.server.dashboard.LocationEditServlet;
import foodtruck.server.dashboard.LocationListServlet;
import foodtruck.server.dashboard.MessageEditServlet;
import foodtruck.server.dashboard.MessageListServlet;
import foodtruck.server.dashboard.NotificationServlet;
import foodtruck.server.dashboard.ObserverServlet;
import foodtruck.server.dashboard.StatsServlet;
import foodtruck.server.dashboard.SyncServlet;
import foodtruck.server.dashboard.TestNotificationServlet;
import foodtruck.server.dashboard.TruckListServlet;
import foodtruck.server.dashboard.TruckServlet;
import foodtruck.server.dashboard.TruckStopServlet;
import foodtruck.server.job.ErrorCountServlet;
import foodtruck.server.job.InvalidateScheduleCache;
import foodtruck.server.job.OneTimeSpecialsSetupServlet;
import foodtruck.server.job.ProfileSyncServlet;
import foodtruck.server.job.PurgeStatsServlet;
import foodtruck.server.job.PushNotificationServlet;
import foodtruck.server.job.RecacheServlet;
import foodtruck.server.job.SendLunchNotificationsServlet;
import foodtruck.server.job.TweetCacheUpdateServlet;
import foodtruck.server.job.TwitterCachePurgeServlet;
import foodtruck.server.job.UpdateAllTrucks;
import foodtruck.server.job.UpdateLocationStats;
import foodtruck.server.job.UpdateTruckStats;
import foodtruck.server.resources.DailySpecialResourceFactory;
import foodtruck.server.vendor.BeaconnaiseServlet;
import foodtruck.server.vendor.VendorCallbackServlet;
import foodtruck.server.vendor.VendorLogoutServlet;
import foodtruck.server.vendor.VendorOffTheRoadServlet;
import foodtruck.server.vendor.VendorRecacheServlet;
import foodtruck.server.vendor.VendorServlet;
import foodtruck.server.vendor.VendorSettingsServlet;
import foodtruck.server.vendor.VendorTwitterRedirectServlet;

/**
 * Wires all the endpoints for the application.
 * @author aviolette
 * @since Jul 12, 2011
 */
public class FoodtruckServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    serve("/cron/specialsSetup").with(OneTimeSpecialsSetupServlet.class);

    // Offline endpoints called via cron-jobs
    serve("/cron/save_all_trucks").with(UpdateAllTrucks.class);
    serve("/cron/push_notifications").with(PushNotificationServlet.class);
    serve("/cron/recache").with(RecacheServlet.class);
    serve("/cron/tweets").with(TweetCacheUpdateServlet.class);
    serve("/cron/tweetPurge").with(TwitterCachePurgeServlet.class);
    serve("/cron/statPurge").with(PurgeStatsServlet.class);
    serve("/cron/notifications").with(SendLunchNotificationsServlet.class);
    serve("/cron/profileSync").with(ProfileSyncServlet.class);
    serve("/cron/updateTruckStats").with(UpdateTruckStats.class);
    serve("/cron/updateLocationStats").with(UpdateLocationStats.class);
    serve("/cron/invalidateCache").with(InvalidateScheduleCache.class);
    serve("/cron/error_stats").with(ErrorCountServlet.class);
    serve("/services/*").with(GuiceContainer.class,
        ImmutableMap.of(PackagesResourceConfig.PROPERTY_PACKAGES, "foodtruck.server.resources"));
    if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
      serve("/cron/hello_world").with(foodtruck.server.job.TestNotificationServlet.class);
    }
    // Dashboard endpoints
    serve("/admin").with(AdminDashboardServlet.class);
    serve("/admin/addresses").with(AddressRuleServlet.class);
    serveRegex("/admin/trucks/[\\S]*/stops/[\\w]*").with(TruckStopServlet.class);
    serve("/admin/trucks/*").with(TruckServlet.class);
    serve("/admin/trucks").with(TruckListServlet.class);
    serve("/admin/locations/*").with(LocationEditServlet.class);
    serve("/admin/locations", "/admin/locations;*").with(LocationListServlet.class);
    serve("/admin/messages/*").with(MessageEditServlet.class);
    serve("/admin/messages").with(MessageListServlet.class);
    serve("/admin/stats").with(StatsServlet.class);
    serve("/admin/sync").with(SyncServlet.class);
    serve("/admin/notifications").with(NotificationServlet.class);
    serve("/admin/applications").with(ApplicationServlet.class);
    serve("/admin/applications/*").with(ApplicationDetailServlet.class);
    serve("/admin/lookouts").with(ObserverServlet.class);
    serve("/admin/notificationTest").with(TestNotificationServlet.class);
    serve("/admin/event_at/*").with(CompoundEventServlet.class);

    // Vendor dashboard endpoints
    serve("/vendor").with(VendorServlet.class);
    serve("/vendor/recache/*").with(VendorRecacheServlet.class);
    serve("/vendor/offtheroad/*").with(VendorOffTheRoadServlet.class);
    serve("/vendor/settings/*").with(VendorSettingsServlet.class);
    serve("/vendor/twitter").with(VendorTwitterRedirectServlet.class);
    serve("/vendor/callback").with(VendorCallbackServlet.class);
    serve("/vendor/logout").with(VendorLogoutServlet.class);

    // Front-page endpoints
    serve("/weekly-schedule").with(WeeklyScheduleServlet.class);
    serve("/popular").with(PopularServlet.class);
    serve("/businesses").with(TruckBusinessesServlet.class);
    serve("/booze").with(BoozeAndTrucksServlet.class);
    serve("/vendor/beaconnaise").with(BeaconnaiseServlet.class);
    serve("/trucks*").with(TrucksServlet.class);
    serve("/about").with(AboutServlet.class);
    serve("/locations*").with(LocationServlet.class);
    serve("/images/*").with(ImageServlet.class);
    serve("/stats/timeline").with(TruckTimelineServlet.class);
    serve("/support").with(SupportServlet.class);
    serve("/support/iphone").with(IPhoneSupportServlet.class);
    serve("/.well-known/acme-challenge/*").with(SSLVerificationServlet.class);
    serveRegex("/[\\w]*").with(FoodTruckServlet.class);

    install(new FactoryModuleBuilder().build(DailySpecialResourceFactory.class));

  }

  @Provides @Named("remote.tweet.update")
  public boolean provideIsTweetUpdateEnabled() {
    return "true".equals(System.getProperty("remote.tweet.update"));
  }

  @Provides @Named("foodtrucklocator.signal.id")
  public String provideFoodTruckLocatorSignalId() {
    return System.getProperty("foodtrucklocator.signal.id");
  }
}
