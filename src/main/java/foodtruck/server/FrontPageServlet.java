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

/**
 * @author aviolette
 * @since 5/8/13
 */
public abstract class FrontPageServlet extends HttpServlet {
  protected final ConfigurationDAO configurationDAO;

  public FrontPageServlet(ConfigurationDAO configDAO) {
    this.configurationDAO = configDAO;
  }

  @Override protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    UserService userService = UserServiceFactory.getUserService();
    req.setAttribute("isAdmin", userService.isUserLoggedIn() && userService.isUserAdmin());
    req.setAttribute("title", "Chicago Food Truck Finder");
    req.setAttribute("signoutUrl", userService.isUserLoggedIn() ? userService.createLogoutURL("/") : null);
    req.setAttribute("user", userService.getCurrentUser());
    doGetProtected(req, resp);
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
