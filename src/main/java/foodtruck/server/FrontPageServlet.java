package foodtruck.server;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;

/**
 * @author aviolette
 * @since 5/8/13
 */
public abstract class FrontPageServlet extends HttpServlet {
  protected final ConfigurationDAO configurationDAO;
  private final StaticConfig staticConfig;

  public FrontPageServlet(ConfigurationDAO configDAO, StaticConfig staticConfig) {
    this.configurationDAO = configDAO;
    this.staticConfig = staticConfig;
  }

  @Override protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    UserService userService = UserServiceFactory.getUserService();
    req.setAttribute("localFrameworks", "true".equals(System.getProperty("use.local.frameworks", "false")));
    req.setAttribute("isAdmin", userService.isUserLoggedIn() && userService.isUserAdmin());
    String title = System.getProperty("foodtrucklocator.title", "Chicago Food Truck Finder");
    req.setAttribute("title", title);
    req.setAttribute("brandTitle", title);
    req.setAttribute("showBoozy", !"false".equals(System.getProperty("foodtrucklocator.showBoozy")));
    req.setAttribute("showWeekly", !"false".equals(System.getProperty("foodtrucklocator.showWeekly")));
    req.setAttribute("showAbout", !"false".equals(System.getProperty("foodtrucklocator.showAbout")));
    req.setAttribute("showStats", !"false".equals(System.getProperty("foodtrucklocator.showStats")));
    req.setAttribute("showBlog", !"false".equals(System.getProperty("foodtrucklocator.showBlog")));
    req.setAttribute("signoutUrl", userService.isUserLoggedIn() ? userService.createLogoutURL("/") : null);
    req.setAttribute("user", userService.getCurrentUser());
    req.setAttribute("signalId", staticConfig.getSignalId());
    doGetProtected(req, resp);
  }

  protected final void flashError(String error, HttpServletResponse resp) {
    resp.setHeader("Set-Cookie", "flash=" + error + ";Path=/");
  }

  protected abstract void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException;

  protected Location getCenter(@Nullable Cookie[] cookies) {
    double lat = 0, lng = 0;
    if (cookies == null) {
      return configurationDAO.find().getCenter();
    }
    for (int i = 0; i < cookies.length; i++) {
      if ("latitude".equals(cookies[i].getName())) {
        lat = Double.valueOf(cookies[i].getValue());
      } else if ("longitude".equals(cookies[i].getName())) {
        lng = Double.valueOf(cookies[i].getValue());
      }
    }
    if (lat != 0 && lng != 0) {
      return Location.builder().lat(lat).lng(lng).build();
    }
    return configurationDAO.find().getCenter();
  }

}
