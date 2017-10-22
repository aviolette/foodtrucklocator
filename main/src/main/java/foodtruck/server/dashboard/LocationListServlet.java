package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.LocationDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette@gmail.com
 * @since 12/19/11
 */
@Singleton
public class LocationListServlet extends HttpServlet {
  private final GeoLocator locator;
  private final LocationDAO locationDAO;

  @Inject
  public LocationListServlet(GeoLocator locator, LocationDAO locationDAO) {
    this.locator = locator;
    this.locationDAO = locationDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    final String jsp = "/WEB-INF/jsp/dashboard/locationDashboard.jsp";
    req = new GuiceHackRequestWrapper(req, jsp);

    String searchField = req.getParameter("q");
    if (!Strings.isNullOrEmpty(searchField)) {
      Location location = locator.locate(searchField, GeolocationGranularity.BROAD);
      if (location == null) {
        location = locationDAO.findByAddress(searchField);
      }
      if (location != null) {
        resp.sendRedirect("/admin/locations/" + location.getKey());
        return;
      }
      // TODO: add error message if we get here
    }
    final List<Location> autocompleteLocations = locationDAO.findAutocompleteLocations();
    req.setAttribute("locations", locationDAO.findLocationNamesAsJson());
    req.setAttribute("allLocations", autocompleteLocations.stream()
        .filter(Location::isPopular)
        .collect(Collectors.toList()));
    req.setAttribute("nav", "locations");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}
