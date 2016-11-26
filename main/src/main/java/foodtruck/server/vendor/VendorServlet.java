package foodtruck.server.vendor;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 10/15/13
 */
@Singleton
public class VendorServlet extends VendorServletSupport {
  private static final String JSP = "/WEB-INF/jsp/vendor/vendordash.jsp";
  private final LocationDAO locationDAO;
  private final BeaconServletHelper helper;

  @Inject
  public VendorServlet(TruckDAO dao, LocationDAO locationDAO, UserService userService,
      Provider<SessionUser> sessionUserProvider, BeaconServletHelper helper) {
    super(dao, userService, sessionUserProvider);
    this.locationDAO = locationDAO;
    this.helper = helper;
  }

  @Override
  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Truck truck) throws ServletException, IOException {
    if (truck != null) {
      helper.seedRequest(req, truck);
    }
    final List<Location> autocompleteLocations = locationDAO.findAutocompleteLocations();
    List<String> locationNames = ImmutableList.copyOf(Iterables.transform(autocompleteLocations, Location.TO_NAME));
    req.setAttribute("locations", new JSONArray(locationNames).toString());
    req.setAttribute("tab", "vendorhome");
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }
}
