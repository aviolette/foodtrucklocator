package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    Location mapCenter = geoLocator.locate(req.getParameter("mapCenter"), GeolocationGranularity.NARROW);
    // TODO: handle null map center
    config = Configuration.builder(config)
        .yahooGeolocationEnabled("on".equals(req.getParameter("yahooGeolocationEnabled")))
        .googleGeolocationEnabled("on".equals(req.getParameter("googleGeolocationEnabled")))
        .tweetUpdateServletEnabled("on".equals(req.getParameter("tweetUpdateServletEnabled")))
        .localTwitterCachingEnabled("on".equals(req.getParameter("localTwitterCachingEnabled")))
        .remoteTwitterCachingEnabled("on".equals(req.getParameter("remoteTwitterCachingEnabled")))
        .remoteTwitterCacheAddress(req.getParameter("remoteTwitterCacheAddress"))
        .googleCalendarAddress(req.getParameter("googleCalendarAddress"))
        .yahooAppId(req.getParameter("yahooAppId"))
        .primaryTwitterList(req.getParameter("primaryTwitterList"))
        .center(mapCenter)
        .build();
    configDAO.save(config);
    resp.sendRedirect("/admin/configuration");
  }
}
