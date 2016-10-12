package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
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
public class AboutServlet extends FrontPageServlet {
  private final TwitterNotificationAccountDAO notificationAccountDAO;
  private final LocationDAO locationDAO;

  @Inject
  public AboutServlet(StaticConfig staticConfig, LocationDAO locationDAO,
      TwitterNotificationAccountDAO notificationAccountDAO) {
    super(staticConfig);
    this.notificationAccountDAO = notificationAccountDAO;
    this.locationDAO = locationDAO;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
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
    req.getRequestDispatcher("/WEB-INF/jsp/about.jsp").forward(req, resp);
  }
}
