package foodtruck.server.vendor;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.server.dashboard.EventServletSupport;
import foodtruck.util.Session;

/**
 * @author aviolette
 * @since 7/13/16
 */
@Singleton
public class LocationEditVendorServlet extends VendorServletSupport {
  private final Provider<EventServletSupport> eventServletSupportProvider;

  @Inject
  public LocationEditVendorServlet(TruckDAO dao, Provider<Session> sessionProvider, UserService userService,
      LocationDAO locationDAO, Provider<EventServletSupport> eventServletSupport) {
    super(dao, sessionProvider, userService, locationDAO);
    this.eventServletSupportProvider = eventServletSupport;
  }

  @Override
  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp, @Nullable Location location) throws ServletException, IOException {
    eventServletSupportProvider.get().get(location);
  }

  @Override
  protected void dispatchPost(HttpServletRequest req, HttpServletResponse resp, Location location,
      String principalName) throws IOException {
    eventServletSupportProvider.get().post(location, principalName, "/vendor/locations/" + location.getKey());
  }
}
