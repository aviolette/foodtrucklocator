package foodtruck.server.vendor;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.util.Session;

/**
 * @author aviolette
 * @since 11/4/13
 */
@Singleton
public class VendorSettingsServlet extends VendorServletSupport {
  private static final String JSP = "/WEB-INF/jsp/vendor/vendorSettings.jsp";

  @Inject
  protected VendorSettingsServlet(TruckDAO dao, Provider<Session> sessionProvider) {
    super(dao, sessionProvider);
  }

  @Override protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp, @Nullable Truck truck)
      throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP);
    req.setAttribute("tab", "trucksettings");
    req.getRequestDispatcher(JSP).forward(req, resp);
  }

  @Override protected void dispatchPost(HttpServletRequest req, HttpServletResponse resp, String truckId)
      throws IOException {
    Truck truck = truckDAO.findById(truckId);
    Escaper escaper = HtmlEscapers.htmlEscaper();
    String name = req.getParameter("name"),
        url = req.getParameter("url"),
        phone = req.getParameter("phone"),
        email = req.getParameter("email"),
        description = req.getParameter("description");
    try {
      Preconditions.checkState(!Strings.isNullOrEmpty(name), "Name cannot be unspecified");
      truck = Truck.builder(truck)
          .name(escaper.escape(name))
          .url(url == null ? null : escaper.escape(url))
          .phone(phone == null ? null : escaper.escape(phone))
          .email(email == null ? null : escaper.escape(email))
          .description(description == null ? null : escaper.escape(description)).build();
      truckDAO.save(truck);
      req.setAttribute("flash", "Successfully saved");
    } catch (Exception e) {
      resp.setStatus(400);
      resp.getWriter().println(e.getMessage());
    }
  }
}
