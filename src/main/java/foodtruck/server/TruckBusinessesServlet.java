package foodtruck.server;

import java.io.IOException;
import java.util.Comparator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ConfigurationDAO;
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
  private final LocationDAO locationDAO;
  private final TruckDAO truckDAO;

  @Inject
  public TruckBusinessesServlet(ConfigurationDAO configDAO, StaticConfig staticConfig, LocationDAO locationDAO,
      TruckDAO truckDAO) {
    super(configDAO, staticConfig);
    this.locationDAO = locationDAO;
    this.truckDAO = truckDAO;
  }

  @Override
  protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setAttribute("locations", FluentIterable.from(locationDAO.findLocationsOwnedByFoodTrucks())
        .transform(new Function<Location, LocationWithTruck>() {
          public LocationWithTruck apply(Location location) {
            return new LocationWithTruck(truckDAO.findById(location.getOwnedBy()), location);
          }
        }).toSortedList(new Comparator<LocationWithTruck>() {
          public int compare(LocationWithTruck o1, LocationWithTruck o2) {
            return o1.getLocation().getName().compareTo(o2.getLocation().getName());
          }
        }));
    req.getRequestDispatcher("/WEB-INF/jsp/businesses.jsp").forward(req, resp);
  }

  public static class LocationWithTruck {
    private final Truck truck;
    private final Location location;

    public LocationWithTruck(Truck truck, Location location) {
      this.truck = truck;
      this.location = location;
    }

    public Truck getTruck() {
      return truck;
    }

    public Location getLocation() {
      return location;
    }
  }
}
