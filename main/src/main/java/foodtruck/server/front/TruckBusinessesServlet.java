package foodtruck.server.front;

import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.server.CodedServletException;

import static foodtruck.server.CodedServletException.NOT_FOUND;

/**
 * @author aviolette
 * @since 1/29/15
 */
@Singleton
public class TruckBusinessesServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(TruckBusinessesServlet.class.getName());
  private final LocationDAO locationDAO;
  private final TruckDAO truckDAO;

  @Inject
  public TruckBusinessesServlet(LocationDAO locationDAO, TruckDAO truckDAO) {
    this.locationDAO = locationDAO;
    this.truckDAO = truckDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setAttribute("locations", locationDAO.findLocationsOwnedByFoodTrucks().stream()
        .map(location -> {
          if (Strings.isNullOrEmpty(location.getOwnedBy())) {
            return null;
          }
          try {
            return new LocationWithTruck(truckDAO.findByIdOpt(location.getOwnedBy()).orElseThrow(NOT_FOUND), location);
          } catch (CodedServletException e) {
            log.log(Level.WARNING, "Problem with location {0}", location);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .sorted(Comparator.comparing(o -> o.getLocation().getName()))
        .collect(Collectors.toList()));
    req.setAttribute("tab", "location");
    req.getRequestDispatcher("/WEB-INF/jsp/businesses.jsp")
        .forward(req, resp);
  }

  // public access needed for access on JSP
  @SuppressWarnings("WeakerAccess")
  public static class LocationWithTruck {
    private final Truck truck;
    private final Location location;

    LocationWithTruck(Truck truck, Location location) {
      this.truck = truck;
      this.location = Location.builder(location)
          .description(sanitize(location.getDescription()))
          .url(Strings.nullToEmpty(location.getUrl()))
          .build();
    }

    private String sanitize(String description) {
      return description.replaceAll("\n", "<br/>");
    }

    public Truck getTruck() {
      return truck;
    }

    public Location getLocation() {
      return location;
    }
  }
}
