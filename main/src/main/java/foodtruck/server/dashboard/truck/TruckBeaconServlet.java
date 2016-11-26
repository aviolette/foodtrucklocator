package foodtruck.server.dashboard.truck;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.util.Link;

/**
 * @author aviolette
 * @since 11/25/16
 */
@Singleton
public class TruckBeaconServlet extends AbstractTruckServlet {
  private static final String JSP = "/WEB-INF/jsp/dashboard/truck/beacons.jsp";

  @Inject
  public TruckBeaconServlet(TruckDAO truckDAO) {
    super(truckDAO);
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
