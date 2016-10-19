package foodtruck.server.vendor;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.dao.LinxupAccountDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.linxup.TruckMonitorService;
import foodtruck.model.LinxupAccount;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.util.ServiceException;
import foodtruck.util.Session;

/**
 * @author aviolette
 * @since 10/18/16
 */
@Singleton
public class VendorLinxupConfigServlet extends VendorServletSupport {
  private static final String JSP = "/WEB-INF/jsp/vendor/linxup.jsp";
  private final LinxupAccountDAO accountDAO;
  private final TruckMonitorService service;

  @Inject
  public VendorLinxupConfigServlet(TruckDAO dao, Provider<Session> sessionProvider, UserService userService,
      LocationDAO locationDAO, LinxupAccountDAO linxupAccountDAO, TruckMonitorService service) {
    super(dao, sessionProvider, userService, locationDAO);
    this.accountDAO = linxupAccountDAO;
    this.service = service;
  }

  @Override
  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Truck truck) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP);
    req.setAttribute("tab", "linxup");
    String username = "";
    if (truck != null) {
      LinxupAccount account = accountDAO.findByTruck(truck.getId());
      if (account != null) {
        username = account.getUsername();
      }
    }
    req.setAttribute("username", username);
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }

  @Override
  protected void dispatchPost(HttpServletRequest req, HttpServletResponse resp, String truckId) throws IOException {
    super.dispatchPost(req, resp, truckId);
    String userName = req.getParameter("username");
    String password = req.getParameter("password");
    if (Strings.isNullOrEmpty(userName) || Strings.isNullOrEmpty(password)) {
      resp.sendError(400);
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
      resp.sendError(400, se.getMessage());
      return;
    }
    resp.sendRedirect("/vendor");
  }
}
