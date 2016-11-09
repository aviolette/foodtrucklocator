package foodtruck.server.vendor;

import java.security.Principal;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
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
  private final Supplier<Principal> principalSupplier = Suppliers.memoize(new Supplier<Principal>() {
    @Override
    public Principal get() {
      Principal principal = (Principal) session.getProperty("principal");
      return principal == null ? request.getUserPrincipal() : principal;
    }
  });

  @Inject
  public SessionUser(Session session, TruckDAO truckDAO, LocationDAO locationDAO, HttpServletRequest request) {
    this.session = session;
    this.truckDAO = truckDAO;
    this.locationDAO = locationDAO;
    this.request = request;
  }

  @Override
  public String toString() {
    Principal principal = getPrincipal();
    return principal == null ? "NOT LOGGED IN" : principal.getName();
  }

  public boolean isLoggedIn() {
    return getPrincipal() != null;
  }

  @Nullable
  public Principal getPrincipal() {
    return principalSupplier.get();
  }

  public Set<Truck> associatedTrucks() {
    Principal principal = getPrincipal();
    if (principal != null) {
      if (isIdentifiedByEmail()) {
        return truckDAO.findByBeaconnaiseEmail(principal.getName()
            .toLowerCase());
      } else {
        return ImmutableSet.copyOf(truckDAO.findByTwitterId(principal.getName()));
      }
    }
    return ImmutableSet.of();
  }

  public boolean isIdentifiedByEmail() {
    return getPrincipal().getName()
        .contains("@");
  }

  Set<Location> associatedLocations() {
    if (isIdentifiedByEmail()) {
      return ImmutableSet.copyOf(locationDAO.findByManagerEmail(getPrincipal().getName()));
    } else {
      return ImmutableSet.copyOf(locationDAO.findByTwitterId(getPrincipal().getName()));
    }
  }

  public void invalidate() {
    session.invalidate();
  }
}
