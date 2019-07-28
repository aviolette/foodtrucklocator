package foodtruck.server.dashboard.truck;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.LinxupAccountDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.linxup.TrackingDeviceService;
import foodtruck.model.LinxupAccount;
import foodtruck.model.Truck;
import foodtruck.util.Link;
import foodtruck.util.ServiceException;

/**
 * Links or unlinks a linxup account to a truck's profile.
 * @author aviolette
 * @since 11/27/16
 */
@Singleton
public class LinxupConfigServlet extends AbstractTruckServlet {
  private final LinxupAccountDAO accountDAO;
  private final TrackingDeviceService service;

  @Inject
  public LinxupConfigServlet(TruckDAO truckDAO, LinxupAccountDAO accountDAO, TrackingDeviceService service, LocationDAO locationDAO) {
    super(truckDAO, locationDAO);
    this.accountDAO = accountDAO;
    this.service = service;
  }

  @Override
  protected void doGetProtected(HttpServletRequest request, HttpServletResponse response,
      Truck truck) throws ServletException, IOException {
    LinxupAccount account = accountDAO.findByTruck(truck.getId());
    String username = "";
    if (account != null) {
      username = account.getUsername();
    }
    request.setAttribute("headerSelection", "beacon");

    request.setAttribute("username", username);
    forward(request, response);
  }

  @Override
  protected void doPostProtected(HttpServletRequest req, HttpServletResponse response,
      Truck truck) throws IOException {
    String truckId = truck.getId();
    if ("Unlink Account".equals(req.getParameter("action"))) {
      LinxupAccount account = accountDAO.findByTruck(truckId);
      accountDAO.delete((Long) account.getKey());
      service.removeDevicesFor(truckId);
      response.sendRedirect("/admin/trucks/" + truckId);
      return;
    }

    String userName = req.getParameter("username");
    String password = req.getParameter("password");
    if (Strings.isNullOrEmpty(userName) || Strings.isNullOrEmpty(password)) {
      flash("Username and password need to be specified", response);
      response.sendRedirect("/admin/trucks/" + truckId + "/linxup_config");
      return;
    }
    LinxupAccount account = accountDAO.findByTruck(truckId);
    if (account == null) {
      account = LinxupAccount.builder()
          .username(userName)
          .password(password)
          .truckId(truckId)
          .build();
    } else {
      account = LinxupAccount.builder(account)
          .username(userName)
          .password(password)
          .build();
    }
    accountDAO.save(account);
    try {
      service.synchronizeFor(account);
    } catch (ServiceException se) {
      flash(se.getMessage(), response);
      response.sendRedirect("/admin/trucks/" + truckId + "/linxup_config");
      return;
    }
    response.sendRedirect("/admin/trucks/" + truckId);
  }

  @Override
  protected ImmutableList<Link> breadcrumbs(Truck truck) {
    return ImmutableList.of(new Link("Trucks", "/admin/trucks"),
        new Link(truck.getName(), "/admin/trucks/" + truck.getId()),
        new Link("Beacons", "/admin/trucks/" + truck.getId() + "/linxup_config"));
  }

  @Override
  protected String getJsp() {
    return "/WEB-INF/jsp/dashboard/truck/linxup.jsp";
  }
}
