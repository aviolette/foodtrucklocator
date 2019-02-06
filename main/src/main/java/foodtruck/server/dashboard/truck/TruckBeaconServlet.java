package foodtruck.server.dashboard.truck;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.annotations.GoogleJavascriptApiKey;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.server.vendor.BeaconServletHelper;
import foodtruck.util.Link;

/**
 * @author aviolette
 * @since 11/25/16
 */
@Singleton
public class TruckBeaconServlet extends AbstractTruckServlet {
  private static final String JSP = "/WEB-INF/jsp/dashboard/truck/beacons.jsp";
  private final BeaconServletHelper helper;
  private final String javascriptApiKey;

  @Inject
  public TruckBeaconServlet(TruckDAO truckDAO, BeaconServletHelper helper,
      @GoogleJavascriptApiKey String javascriptApiKey) {
    super(truckDAO);
    this.helper = helper;
    this.javascriptApiKey = javascriptApiKey;
  }

  @Override
  protected void doGetProtected(HttpServletRequest request, HttpServletResponse response,
      Truck truck) throws ServletException, IOException {
    helper.seedRequest(request, truck);
    if (request.getAttribute("linxupAccount") == null) {
      response.sendRedirect("/admin/trucks/"+ truck.getId() + "/linxup_config");
      return;
    }
    request.setAttribute("extraScripts", ImmutableList.of("//maps.googleapis.com/maps/api/js?key=" + javascriptApiKey,
        "/script/vendordash.js",
        "/script/lib/spin.min.js",
        "/script/dashboard-truck-beacons.js"));
    forward(request, response);
  }

  @Override
  protected ImmutableList<Link> breadcrumbs(Truck truck) {
    return ImmutableList.of(new Link("Trucks", "/admin/trucks"),
        new Link(truck.getName(), "/admin/trucks/" + truck.getId()),
        new Link("Beacons", "/admin/trucks/" + truck.getId() + "/beacons"));
  }

  @Override
  protected String getJsp() {
    return JSP;
  }
}
