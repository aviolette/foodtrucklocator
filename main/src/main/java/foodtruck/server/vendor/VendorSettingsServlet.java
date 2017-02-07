package foodtruck.server.vendor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.util.FormDataMassager;

/**
 * @author aviolette
 * @since 11/4/13
 */
@Singleton
public class VendorSettingsServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(VendorSettingsServlet.class.getName());
  private static final String JSP = "/WEB-INF/jsp/vendor/vendorSettings.jsp";
  private final TruckDAO truckDAO;

  @Inject
  public VendorSettingsServlet(TruckDAO truckDAO) {
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
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
    Escaper escaper = HtmlEscapers.htmlEscaper();
    String name = req.getParameter("name"),
        url = req.getParameter("url"),
        phone = req.getParameter("phone"),
        email = req.getParameter("email"),
        description = req.getParameter("description");
    try {
      Preconditions.checkState(!Strings.isNullOrEmpty(name), "Name cannot be unspecified");
      if (!Strings.isNullOrEmpty(phone)) {
        Preconditions.checkState(FormDataMassager.normalizePhone(phone)
            .length() == 12, "Phone must be in formation XXX-XXX-XXXX");
      }
      url = url == null ? null : escaper.escape(url);
      if (!Strings.isNullOrEmpty(url) && !url.startsWith("http")) {
        url = "http://" + url;
      }
      truck = Truck.builder(truck)
          .name(escaper.escape(name))
          .url(url)
          .normalizePhone(escaper.escape(Strings.nullToEmpty(phone)))
          .email(email == null ? null : escaper.escape(email))
          .description(description == null ? null : escaper.escape(description))
          .build();
      log.log(Level.INFO, "Saving truck {0}", truck);
      truckDAO.save(truck);
      req.setAttribute("flash", "Successfully saved");
    } catch (Exception e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      resp.setStatus(400);
      resp.getWriter()
          .println(e.getMessage());
    }
  }
}
