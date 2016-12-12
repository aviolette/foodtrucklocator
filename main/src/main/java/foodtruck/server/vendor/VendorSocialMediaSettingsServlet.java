package foodtruck.server.vendor;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;

/**
 * @author aviolette
 * @since 11/6/16
 */
@Singleton
public class VendorSocialMediaSettingsServlet extends HttpServlet {
  private static final String JSP = "/WEB-INF/jsp/vendor/socialMediaSettings.jsp";
  private final TruckDAO truckDAO;

  @Inject
  public VendorSocialMediaSettingsServlet(TruckDAO dao) {
    this.truckDAO = dao;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP);
    req.getRequestDispatcher(JSP)
        .forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
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