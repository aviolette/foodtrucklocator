package foodtruck.server.front;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.TwitterNotificationAccount;

/**
 * @author aviolette
 * @since 8/19/14
 */
@Singleton
public class AboutServlet extends HttpServlet {
  private final TwitterNotificationAccountDAO notificationAccountDAO;
  private final LocationDAO locationDAO;
  private final StaticConfig staticConfig;

  @Inject
  public AboutServlet(StaticConfig staticConfig, LocationDAO locationDAO,
      TwitterNotificationAccountDAO notificationAccountDAO) {
    this.notificationAccountDAO = notificationAccountDAO;
    this.locationDAO = locationDAO;
    this.staticConfig = staticConfig;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    ImmutableSet.Builder<TwitterNotificationAccount> builder = ImmutableSet.builder();
    for (TwitterNotificationAccount account : notificationAccountDAO.findAll()) {
      if (account.isActive()) {
        Location location = locationDAO.findByAddress(account.getLocation()
            .getName());
        builder.add(TwitterNotificationAccount.builder(account)
            .location(location)
            .build());
      }
    }
    req.setAttribute("accounts", builder.build());
    req.setAttribute("tab", "about");
    req.setAttribute("accountCenter", staticConfig.getCenter());
    req.getRequestDispatcher("/WEB-INF/jsp/about.jsp")
        .forward(req, resp);
  }
}
