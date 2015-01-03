package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ApplicationDAO;
import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Application;
import foodtruck.model.Configuration;
import foodtruck.model.Location;
import foodtruck.schedule.Confidence;
import foodtruck.twitter.ProfileSyncService;
import foodtruck.util.RandomString;

/**
 * @author aviolette@gmail.com
 * @since 4/10/12
 */
@Singleton
public class ConfigurationServlet extends HttpServlet {
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/configuration.jsp";
  private final ConfigurationDAO configDAO;
  private final GeoLocator geoLocator;
  private final ApplicationDAO applicationDAO;
  private final TruckDAO truckDAO;
  private final ProfileSyncService profileSyncService;

  @Inject
  public ConfigurationServlet(ConfigurationDAO configDAO, GeoLocator geoLocator, ApplicationDAO applicationDAO,
      TruckDAO truckDAO, ProfileSyncService syncService) {
    this.configDAO = configDAO;
    this.geoLocator = geoLocator;
    this.applicationDAO = applicationDAO;
    this.truckDAO = truckDAO;
    this.profileSyncService = syncService;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Configuration config = configDAO.find();
    config = initialSetup(config);
    req.setAttribute("config", config);
    req.setAttribute("nav", "settings");
    req.getRequestDispatcher(JSP_PATH).forward(req, resp);
  }

  private Configuration initialSetup(Configuration config) {
    Configuration.Builder builder = null;
    if (config.getCenter() == null) {
      builder = Configuration.builder(config);
      builder.center(Location.builder().name("Clark and Monroe, Chicago, IL").lat(41.889973).lng(-87.634024).build());
    }
    if (Strings.isNullOrEmpty(config.getFrontDoorAppKey())) {
      if (builder == null) {
        builder = Configuration.builder(config);
      }
      if (applicationDAO.findAll().isEmpty()) {
        try {
          Application application = Application.builder().name("Front Door").description("Front Door Application Key").enabled(true).appKey(
              RandomString.nextString(8)).build();
          applicationDAO.save(application);
          builder.frontDoorAppKey(application.getAppKey());
        } catch (Exception e) {
          log(e.getMessage(), e);
        }
      }
    }
    if (builder != null) {
      config = builder.build();
      configDAO.save(config);
    }
    return config;
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Configuration config = configDAO.find();
    List<String> notificationReceivers = ImmutableList.copyOf(
        Splitter.on(",").trimResults().omitEmptyStrings().split(req.getParameter("notificationReceivers")).iterator());
    Location mapCenter = geoLocator.locate(req.getParameter("mapCenter"), GeolocationGranularity.NARROW);
    // TODO: handle null map center
    config = Configuration.builder(config)
        .minimumConfidenceForDisplay(Confidence.LOW)
        .autoOffRoad("on".equals(req.getParameter("autoOffRoad")))
        .showPublicTruckGraphs("on".equals(req.getParameter("showPublicTruckGraphs")))
        .foodTruckRequestOn("on".equals(req.getParameter("foodTruckRequestOn")))
        .syncUrl(req.getParameter("syncUrl"))
        .syncAppKey(req.getParameter("syncAppKey"))
        .baseUrl(req.getParameter("baseUrl"))
        .globalRecachingEnabled("on".equals(req.getParameter("recachingEnabled")))
        .yahooGeolocationEnabled("on".equals(req.getParameter("yahooGeolocationEnabled")))
        .googleGeolocationEnabled("on".equals(req.getParameter("googleGeolocationEnabled")))
        .tweetUpdateServletEnabled("on".equals(req.getParameter("tweetUpdateServletEnabled")))
        .localTwitterCachingEnabled("on".equals(req.getParameter("localTwitterCachingEnabled")))
        .remoteTwitterCachingEnabled("on".equals(req.getParameter("remoteTwitterCachingEnabled")))
        .remoteTwitterCacheAddress(req.getParameter("remoteTwitterCacheAddress"))
        .sendNotificationTweetWhenNoTrucks("on".equals(req.getParameter("sendNotificationTweetWhenNoTrucks")))
        .retweetStopCreatingTweets("on".equals(req.getParameter("retweetStopCreatingTweets")))
        .googleCalendarAddress(req.getParameter("googleCalendarAddress"))
        .yahooAppId(req.getParameter("yahooAppId"))
        .scheduleCachingOn("on".equals(req.getParameter("scheduleCachingOn")))
        .primaryTwitterList(req.getParameter("primaryTwitterList"))
        .notificationSender(req.getParameter("notificationSender"))
        .frontDoorAppKey(req.getParameter("frontDoorAppKey"))
        .systemNotificationList(notificationReceivers)
        .center(mapCenter)
        .build();

    long count = truckDAO.count();
    if (count == 0 && !Strings.isNullOrEmpty(config.getPrimaryTwitterList())) {
      profileSyncService.syncFromTwitterList(config.getPrimaryTwitterList());
    }
    configDAO.save(config);
    resp.sendRedirect("/admin/configuration");
  }
}
