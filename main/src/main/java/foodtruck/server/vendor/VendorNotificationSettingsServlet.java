package foodtruck.server.vendor;

import java.io.IOException;
import java.util.Set;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette
 * @since 1/11/17
 */
@Singleton
public class VendorNotificationSettingsServlet extends HttpServlet {

  private static final String JSP = "/WEB-INF/jsp/vendor/notificationSettings.jsp";
  private final TruckDAO truckDAO;

  @Inject
  public VendorNotificationSettingsServlet(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP);
    req.setAttribute("tab", "profile");
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
    final String[] optionsArray = req.getParameterValues("options");
    Set<String> options = ImmutableSet.copyOf(optionsArray == null ? new String[0] : optionsArray);
    truck = Truck.builder(truck)
        .notifyOfLocationChanges(options.contains("notifyOfLocationChanges"))
        .build();
    truckDAO.save(truck);
  }
}
