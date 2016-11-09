package foodtruck.server.vendor;

import java.security.Principal;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.util.Session;

/**
 * @author aviolette
 * @since 11/8/16
 */
@RequestScoped
public class SessionUser {
  private final Session session;
  private final TruckDAO truckDAO;
  private final LocationDAO locationDAO;
  private final HttpServletRequest request;

  @Inject
  public SessionUser(Session session, TruckDAO truckDAO, LocationDAO locationDAO, HttpServletRequest request) {
    this.session = session;
    this.truckDAO = truckDAO;
    this.locationDAO = locationDAO;
    this.request = request;
  }

  @Nullable
  public Principal getPrincipal() {
    Principal principal = (Principal) session.getProperty("principal");
    return principal == null ? request.getUserPrincipal() : principal;
  }

  public Set<Truck> associatedTrucks(Principal principal) {
    if (principal != null) {
      if (isIdentifiedByEmail(principal)) {
        return truckDAO.findByBeaconnaiseEmail(principal.getName()
            .toLowerCase());
      } else {
        return ImmutableSet.copyOf(truckDAO.findByTwitterId(principal.getName()));
      }
    }
    return ImmutableSet.of();
  }

  public boolean isIdentifiedByEmail(Principal principal) {
    return principal.getName()
        .contains("@");
  }

  Set<Location> associatedLocations(Principal principal) {
    if (isIdentifiedByEmail(principal)) {
      return ImmutableSet.copyOf(locationDAO.findByManagerEmail(principal.getName()));
    } else {
      return ImmutableSet.copyOf(locationDAO.findByTwitterId(principal.getName()));
    }
  }

  public void invalidate() {
    session.invalidate();
  }
}
