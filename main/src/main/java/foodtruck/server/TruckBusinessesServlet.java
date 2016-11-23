package foodtruck.server;

import java.io.IOException;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 1/29/15
 */
@Singleton
public class TruckBusinessesServlet extends FrontPageServlet {
  private static final Logger log = Logger.getLogger(TruckBusinessesServlet.class.getName());
  private final LocationDAO locationDAO;
  private final TruckDAO truckDAO;

  @Inject
  public TruckBusinessesServlet(StaticConfig staticConfig, LocationDAO locationDAO, TruckDAO truckDAO,
      Provider<UserService> userServiceProvider) {
    super(staticConfig, userServiceProvider);
    this.locationDAO = locationDAO;
    this.truckDAO = truckDAO;
  }

  @Override
  protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setAttribute("locations", FluentIterable.from(locationDAO.findLocationsOwnedByFoodTrucks())
        .transform(new Function<Location, LocationWithTruck>() {
          public LocationWithTruck apply(Location location) {
            try {
              //TODO: this shouldn't happen
              if (Strings.isNullOrEmpty(location.getOwnedBy())) {
                return null;
              }
              return new LocationWithTruck(truckDAO.findById(location.getOwnedBy()), location);
            } catch (Exception e) {
              log.log(Level.WARNING, "Problem with location {0}", location);
              return null;
            }
          }

        })
        .filter(Predicates.notNull())
        .toSortedList(new Comparator<LocationWithTruck>() {
          public int compare(LocationWithTruck o1, LocationWithTruck o2) {
            return o1.getLocation()
                .getName()
                .compareTo(o2.getLocation()
                    .getName());
          }
        }));
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
