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

import foodtruck.model.Location;
import foodtruck.model.StaticConfig;

/**
 * @author aviolette
 * @since 5/8/13
 */
public abstract class FrontPageServlet extends HttpServlet {
  protected final StaticConfig staticConfig;

  public FrontPageServlet(StaticConfig staticConfig) {
    this.staticConfig = staticConfig;
  }

  @Override
  protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // TODO: inject Provider<UserService>; not doing now since it would require updating a gazillion files
    UserService userService = UserServiceFactory.getUserService();
    req.setAttribute("localFrameworks", "true".equals(System.getProperty("use.local.frameworks", "false")));
    req.setAttribute("isAdmin", userService.isUserLoggedIn() && userService.isUserAdmin());
    String title = System.getProperty("foodtrucklocator.title", "Chicago Food Truck Finder");
    req.setAttribute("title", title);
    req.setAttribute("brandTitle", title);
    req.setAttribute("suffix", "-fluid");
    req.setAttribute("googleApiKey", staticConfig.getGoogleJavascriptApiKey());
    req.setAttribute("showBoozy", !"false".equals(System.getProperty("foodtrucklocator.showBoozy")));
    req.setAttribute("showWeekly", !"false".equals(System.getProperty("foodtrucklocator.showWeekly")));
    req.setAttribute("showAbout", !"false".equals(System.getProperty("foodtrucklocator.showAbout")));
    req.setAttribute("showStats", !"false".equals(System.getProperty("foodtrucklocator.showStats")));
    req.setAttribute("showBlog", !"false".equals(System.getProperty("foodtrucklocator.showBlog")));
    req.setAttribute("twitterHandle", System.getProperty("foodtrucklocator.twitter.handle", "chifoodtruckz"));
    req.setAttribute("facebookPage", System.getProperty("foodtrucklocator.facebook.page", "chicagofoodtruckfinder"));
    req.setAttribute("signoutUrl", userService.isUserLoggedIn() ? userService.createLogoutURL("/") : null);
    req.setAttribute("user", userService.getCurrentUser());
    req.setAttribute("signalId", staticConfig.getSignalId());
    doGetProtected(req, resp);
  }

  protected abstract void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException;

  Location getCenter(@Nullable Cookie[] cookies) {
    double lat = 0, lng = 0;
    if (cookies == null) {
      return staticConfig.getCenter();
    }
    for (Cookie cooky : cookies) {
      if ("latitude".equals(cooky.getName())) {
        lat = Double.valueOf(cooky.getValue());
      } else if ("longitude".equals(cooky.getName())) {
        lng = Double.valueOf(cooky.getValue());
      }
    }
    if (lat != 0 && lng != 0) {
      return Location.builder().lat(lat).lng(lng).build();
    }
    return staticConfig.getCenter();
  }
}
