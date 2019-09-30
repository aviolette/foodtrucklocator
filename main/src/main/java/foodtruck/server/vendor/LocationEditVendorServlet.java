package foodtruck.server.vendor;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.model.Location;
import foodtruck.server.dashboard.EventServletSupport;

import static foodtruck.server.vendor.VendorPageFilter.LOCATION;
import static foodtruck.server.vendor.VendorPageFilter.PRINCIPAL;

@Singleton
public class LocationEditVendorServlet extends HttpServlet {
  private final Provider<EventServletSupport> eventServletSupportProvider;

  @Inject
  public LocationEditVendorServlet( Provider<EventServletSupport> eventServletSupport) {
    this.eventServletSupportProvider = eventServletSupport;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    eventServletSupportProvider.get().get((Location)req.getAttribute(LOCATION), "/vendor/managed-location");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    eventServletSupportProvider.get().post((Location)req.getAttribute(LOCATION),
        ((Principal) req.getAttribute(PRINCIPAL)).getName(), "/vendor/managed-location");
  }
}
