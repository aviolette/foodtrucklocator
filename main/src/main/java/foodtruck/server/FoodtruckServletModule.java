package foodtruck.server;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import foodtruck.jersey.ScheduleCacherImpl;
import foodtruck.schedule.ScheduleCacher;
import foodtruck.server.book.BookingLandingServlet;
import foodtruck.server.book.CreateAccountServlet;
import foodtruck.server.book.LoginServlet;
import foodtruck.server.book.LogoutServlet;
import foodtruck.server.book.PrepaidServlet;
import foodtruck.server.dashboard.AddressRuleServlet;
import foodtruck.server.dashboard.AdminDashboardServlet;
import foodtruck.server.dashboard.AlexaQueryServlet;
import foodtruck.server.dashboard.AlexaTestServlet;
import foodtruck.server.dashboard.ApplicationDetailServlet;
import foodtruck.server.dashboard.ApplicationServlet;
import foodtruck.server.dashboard.BeaconServlet;
import foodtruck.server.dashboard.BeaconsServlet;
import foodtruck.server.dashboard.CompoundEventServlet;
import foodtruck.server.dashboard.LocationEditServlet;
import foodtruck.server.dashboard.LocationListServlet;
import foodtruck.server.dashboard.MessageEditServlet;
import foodtruck.server.dashboard.MessageListServlet;
import foodtruck.server.dashboard.NotificationServlet;
import foodtruck.server.dashboard.NotificationSetupServlet;
import foodtruck.server.dashboard.ObserverServlet;
import foodtruck.server.dashboard.StatsServlet;
import foodtruck.server.dashboard.SyncServlet;
import foodtruck.server.dashboard.TestNotificationServlet;
import foodtruck.server.dashboard.TruckListServlet;
import foodtruck.server.dashboard.TruckStopServlet;
import foodtruck.server.dashboard.VersionInfo;
import foodtruck.server.dashboard.truck.LinxupConfigServlet;
import foodtruck.server.dashboard.truck.OffTheRoadServlet;
import foodtruck.server.dashboard.truck.TruckBeaconServlet;
import foodtruck.server.dashboard.truck.TruckConfigurationServlet;
import foodtruck.server.dashboard.truck.TruckServlet;
import foodtruck.server.dashboard.truck.TruckStatsServlet;
import foodtruck.server.front.AboutServlet;
import foodtruck.server.front.BoozeAndTrucksServlet;
import foodtruck.server.front.FoodTruckServlet;
import foodtruck.server.front.IPhoneSupportServlet;
import foodtruck.server.front.LocationServlet;
import foodtruck.server.front.PopularServlet;
import foodtruck.server.front.SSLVerificationServlet;
import foodtruck.server.front.SupportServlet;
import foodtruck.server.front.TruckBusinessesServlet;
import foodtruck.server.front.TruckTimelineServlet;
import foodtruck.server.front.TrucksServlet;
import foodtruck.server.front.WeeklyScheduleServlet;
import foodtruck.server.job.CheckDeviceIssuesServlet;
import foodtruck.server.job.ErrorCountServlet;
import foodtruck.server.job.InvalidateScheduleCache;
import foodtruck.server.job.NotifyLeavingStopServlet;
import foodtruck.server.job.NotifyNewStopServlet;
import foodtruck.server.job.ProfileSyncServlet;
import foodtruck.server.job.PurgeStatsServlet;
import foodtruck.server.job.RecacheServlet;
import foodtruck.server.job.SendLunchNotificationsServlet;
import foodtruck.server.job.StatUpdateQueueServlet;
import foodtruck.server.job.TruckMonitorServlet;
import foodtruck.server.job.TweetCacheUpdateServlet;
import foodtruck.server.job.TwitterCachePurgeServlet;
import foodtruck.server.job.UpdateLocationStats;
import foodtruck.server.job.UpdateTruckStats;
import foodtruck.server.resources.DailySpecialResourceFactory;
import foodtruck.server.vendor.DeviceInfoServlet;
import foodtruck.server.vendor.MenuServlet;
import foodtruck.server.vendor.PostScheduleServlet;
import foodtruck.server.vendor.VendorBeaconDetailsServlet;
import foodtruck.server.vendor.VendorCallbackServlet;
import foodtruck.server.vendor.VendorEditStopServlet;
import foodtruck.server.vendor.VendorInfoServlet;
import foodtruck.server.vendor.VendorLocationEditServlet;
import foodtruck.server.vendor.VendorLogoutServlet;
import foodtruck.server.vendor.VendorNotificationSettingsServlet;
import foodtruck.server.vendor.VendorOffTheRoadServlet;
import foodtruck.server.vendor.VendorPageFilter;
import foodtruck.server.vendor.VendorRecacheServlet;
import foodtruck.server.vendor.VendorServlet;
import foodtruck.server.vendor.VendorSettingsServlet;
import foodtruck.server.vendor.VendorSocialMediaSettingsServlet;
import foodtruck.server.vendor.VendorTwitterRedirectServlet;
import foodtruck.server.vendor.VendorUnlinkAccountServlet;

/**
 * Wires all the endpoints for the application.
 * @author aviolette
 * @since Jul 12, 2011
 */
class FoodtruckServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    // Offline endpoints called via cron-jobs
    serve("/cron/recache").with(RecacheServlet.class);
    serve("/cron/tweets").with(TweetCacheUpdateServlet.class);
    serve("/cron/tweetPurge").with(TwitterCachePurgeServlet.class);
    serve("/cron/statPurge").with(PurgeStatsServlet.class);
    serve("/cron/notifications").with(SendLunchNotificationsServlet.class);
    serve("/cron/updateTruckStats").with(UpdateTruckStats.class);
    serve("/cron/updateLocationStats").with(UpdateLocationStats.class);
    serve("/cron/error_stats").with(ErrorCountServlet.class);
    serve("/cron/activate_beacon").with(TruckMonitorServlet.class);
    serve("/cron/check_device_issues").with(CheckDeviceIssuesServlet.class);

    // Queue activated servlets
    serve("/cron/update_count").with(StatUpdateQueueServlet.class);
    serve("/cron/notify_stop_created").with(NotifyNewStopServlet.class);
    serve("/cron/notify_stop_ended").with(NotifyLeavingStopServlet.class);

    // Dashboard endpoints
    serve("/admin").with(AdminDashboardServlet.class);
    serve("/admin/alexa_test").with(AlexaTestServlet.class);
    serve("/admin/alexa_query").with(AlexaQueryServlet.class);
    serve("/admin/addresses").with(AddressRuleServlet.class);
    serveRegex("/admin/beacons/[\\w]+").with(BeaconServlet.class);
    serve("/admin/beacons").with(BeaconsServlet.class);
    serveRegex("/admin/trucks/[\\S]*/stops/[\\w]*").with(TruckStopServlet.class);
    serveRegex("/admin/trucks/[\\S]*/menu").with(foodtruck.server.dashboard.MenuServlet.class);
    serveRegex("/admin/trucks/[\\S]*/configuration").with(TruckConfigurationServlet.class);
    serveRegex("/admin/trucks/[\\S]*/stats").with(TruckStatsServlet.class);
    serveRegex("/admin/trucks/[\\S]*/beacons").with(TruckBeaconServlet.class);
    serveRegex("/admin/trucks/[\\S]*/offtheroad").with(OffTheRoadServlet.class);
    serveRegex("/admin/trucks/[\\S]*/linxup_config").with(LinxupConfigServlet.class);
    serve("/admin/trucks/*").with(TruckServlet.class);
    serve("/admin/trucks").with(TruckListServlet.class);
    serve("/admin/locations/*").with(LocationEditServlet.class);
    serve("/admin/locations", "/admin/locations;*").with(LocationListServlet.class);
    serve("/admin/messages/*").with(MessageEditServlet.class);
    serve("/admin/messages").with(MessageListServlet.class);
    serve("/admin/stats").with(StatsServlet.class);
    serve("/admin/sync").with(SyncServlet.class);
    serve("/admin/notifications").with(NotificationServlet.class);
    serve("/admin/notifications/new").with(NotificationSetupServlet.class);
    serve("/admin/applications").with(ApplicationServlet.class);
    serve("/admin/applications/*").with(ApplicationDetailServlet.class);
    serve("/admin/lookouts").with(ObserverServlet.class);
    serve("/admin/notificationTest").with(TestNotificationServlet.class);
    serve("/admin/event_at/*").with(CompoundEventServlet.class);
    serve("/admin/profileSync").with(ProfileSyncServlet.class);
    serve("/admin/invalidateCache").with(InvalidateScheduleCache.class);
    serve("/admin/versioninfo").with(VersionInfo.class);

    // Vendor dashboard endpoints
    serve("/vendor").with(VendorServlet.class);
    serveRegex("/vendor/locations/[\\d]*/edit").with(VendorLocationEditServlet.class);
    serveRegex("/vendor/stops/[\\w]*").with(VendorEditStopServlet.class);
    serve("/vendor/recache/*").with(VendorRecacheServlet.class);
    serve("/vendor/offtheroad/*").with(VendorOffTheRoadServlet.class);
    serve("/vendor/settings/*").with(VendorSettingsServlet.class);
    serve("/vendor/twitter").with(VendorTwitterRedirectServlet.class);
    serve("/vendor/callback").with(VendorCallbackServlet.class);
    serve("/vendor/logout").with(VendorLogoutServlet.class);
    serve("/vendor/menu/*").with(MenuServlet.class);
    serve("/vendor/beacons/*").with(VendorBeaconDetailsServlet.class);
    serveRegex("/vendor/socialmedia/[\\S]*/unlink").with(VendorUnlinkAccountServlet.class);
    serve("/vendor/socialmedia/*").with(VendorSocialMediaSettingsServlet.class);
    serve("/vendor/schedule").with(PostScheduleServlet.class);
    serve("/vendor/notifications/*").with(VendorNotificationSettingsServlet.class);

    // Alexa integration
    serve("/amazonalexa").with(AlexaServlet.class);

    if ("true".equals(System.getProperty("foodtrucklocator.supports.booking"))) {
      // Booking endpoints
      serve("/book").with(BookingLandingServlet.class);
      serve("/login").with(LoginServlet.class);
      serve("/logout").with(LogoutServlet.class);
      serve("/book/prepaid").with(PrepaidServlet.class);
      serve("/book/create_account").with(CreateAccountServlet.class);
    }

    // Front-page endpoints
    serve("/vendinfo").with(VendorInfoServlet.class);
    serve("/vendinfo/device").with(DeviceInfoServlet.class);
    serve("/weekly-schedule").with(WeeklyScheduleServlet.class);
    serve("/popular").with(PopularServlet.class);
    serve("/businesses").with(TruckBusinessesServlet.class);
    serve("/booze").with(BoozeAndTrucksServlet.class);
    serve("/trucks*").with(TrucksServlet.class);
    serve("/about").with(AboutServlet.class);
    serve("/locations*").with(LocationServlet.class);
    serve("/images/*").with(ImageServlet.class);
    serve("/stats/timeline").with(TruckTimelineServlet.class);
    serve("/support").with(SupportServlet.class);
    serve("/support/iphone").with(IPhoneSupportServlet.class);
    serve("/.well-known/acme-challenge/*").with(SSLVerificationServlet.class);
    serve("/").with(FoodTruckServlet.class);

    // Services
    install(new FactoryModuleBuilder().build(DailySpecialResourceFactory.class));
    serve("/services/*").with(GuiceContainer.class,
        ImmutableMap.of(PackagesResourceConfig.PROPERTY_PACKAGES, "foodtruck.server.resources"));

    // Filters
    if ("true".equals(System.getProperty("foodtrucklocator.supports.ssl"))) {
      filterRegex("/admin/.*", "/", "/vendor.*", "/book.*").through(SSLRedirectFilter.class);
    }
    filterRegex("/", "/popular.*", "/businesses.*", "/booze.*", "/trucks.*", "/about.*", "/locations.*",
        "/stats/timeline", "/support.*", "/vendinfo.*").through(PublicPageFilter.class);
    filterRegex("/vendor.*").through(VendorPageFilter.class);
    filter("/*").through(SiteScraperFilter.class);
    filter("/*").through(CommonConfigFilter.class);
  }

  @Provides
  @Named("remote.tweet.update")
  public boolean provideIsTweetUpdateEnabled() {
    return "true".equals(System.getProperty("remote.tweet.update"));
  }

  @Provides
  @Named("foodtrucklocator.signal.id")
  public String provideFoodTruckLocatorSignalId() {
    return System.getProperty("foodtrucklocator.signal.id");
  }

  // TODO: once we're off jersey-json move this into core-services
  @Provides
  public ScheduleCacher providesCacher(ScheduleCacherImpl impl) {
    return impl;
  }
}
