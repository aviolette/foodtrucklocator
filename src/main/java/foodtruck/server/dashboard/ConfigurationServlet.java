package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Configuration;
import foodtruck.model.Location;

/**
 * @author aviolette@gmail.com
 * @since 4/10/12
 */
@Singleton
public class ConfigurationServlet extends HttpServlet {
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/configuration.jsp";
  private final ConfigurationDAO configDAO;
  private final GeoLocator geoLocator;

  @Inject
  public ConfigurationServlet(ConfigurationDAO configDAO, GeoLocator geoLocator) {
    this.configDAO = configDAO;
    this.geoLocator = geoLocator;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Configuration config = configDAO.find();
    req.setAttribute("config", config);
    req.setAttribute("nav", "settings");
    req.getRequestDispatcher(JSP_PATH).forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Configuration config = configDAO.find();
    List<String> notificationReceivers = ImmutableList.copyOf(
        Splitter.on(",").trimResults().omitEmptyStrings().split(req.getParameter("notificationReceivers")).iterator());
    Location mapCenter = geoLocator.locate(req.getParameter("mapCenter"), GeolocationGranularity.NARROW);
    // TODO: handle null map center
    config = Configuration.builder(config)
        .autoOffRoad("on".equals(req.getParameter("autoOffRoad")))
        .showPublicTruckGraphs("on".equals(req.getParameter("showPublicTruckGraphs")))
        .foodTruckRequestOn("on".equals(req.getParameter("foodTruckRequestOn")))
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
        .yahooConsumerKey(req.getParameter("yahooConsumerKey"))
        .yahooConsumerSecret(req.getParameter("yahooConsumerSecret"))
        .primaryTwitterList(req.getParameter("primaryTwitterList"))
        .notificationSender(req.getParameter("notificationSender"))
        .frontDoorAppKey(req.getParameter("frontDoorAppKey"))
        .systemNotificationList(notificationReceivers)
        .center(mapCenter)
        .build();
    configDAO.save(config);
    resp.sendRedirect("/admin/configuration");
  }
}
