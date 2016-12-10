package foodtruck.server.vendor;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette
 * @since 11/6/16
 */
@Singleton
public class VendorSocialMediaSettingsServlet extends VendorServletSupport {
  private static final String JSP = "/WEB-INF/jsp/vendor/socialMediaSettings.jsp";

  @Inject
  public VendorSocialMediaSettingsServlet(TruckDAO dao, UserService userService,
      Provider<SessionUser> sessionUserProvider) {
    super(dao, userService, sessionUserProvider);
  }

  @Override
  protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp,
      @Nullable Truck truck) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP);
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }

  @Override
  protected void dispatchPost(HttpServletRequest req, HttpServletResponse resp, String truckId, Principal principal) throws IOException {
    Truck truck = truckDAO.findById(truckId);
    final String[] optionsArray = req.getParameterValues("options");
    Set<String> options = ImmutableSet.copyOf(optionsArray == null ? new String[0] : optionsArray);
    truck = Truck.builder(truck)
        .postAtNewStop(options.contains("postAtNewStop"))
        .postDailySchedule(options.contains("dailySchedule"))
        .postWeeklySchedule(options.contains("weeklySchedule"))
        .build();
    truckDAO.save(truck);
  }
}