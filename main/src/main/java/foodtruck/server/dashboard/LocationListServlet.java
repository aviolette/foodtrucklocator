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

import static foodtruck.server.CodedServletException.NOT_FOUND;

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
        location = locationDAO.findByName(searchField).orElseThrow(NOT_FOUND);
      }
      resp.sendRedirect("/admin/locations/" + location.getKey());
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
