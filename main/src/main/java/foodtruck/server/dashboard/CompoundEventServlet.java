package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 1/8/15
 */
@Singleton
public class CompoundEventServlet extends HttpServlet {
  private final LocationDAO locationDAO;
  private final Provider<UserService> userServiceProvider;
  private final Provider<EventServletSupport> eventServletSupportProvider;

  @Inject
  public CompoundEventServlet(LocationDAO locationDAO, Provider<UserService> userServiceProvider, Provider<EventServletSupport> eventServletSupportProvider) {
    this.locationDAO = locationDAO;
    this.userServiceProvider = userServiceProvider;
    this.eventServletSupportProvider = eventServletSupportProvider;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Location location = locationDAO.findById(Long.valueOf(req.getRequestURI().substring(
        req.getRequestURI().lastIndexOf('/') + 1)));
    if (location == null) {
      resp.sendError(404);
      return;
    }
    UserService userService = userServiceProvider.get();
    eventServletSupportProvider.get().post(location, userService.getCurrentUser().getEmail(), "/admin/trucks");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String locationID = req.getRequestURI().substring(req.getRequestURI().lastIndexOf('/') + 1);
    Location location = locationDAO.findById(Long.valueOf(locationID));
    if (location == null) {
      resp.sendError(404);
      return;
    }
    eventServletSupportProvider.get().get(location, "/admin/locations/" + locationID);
  }
}
