package foodtruck.server.dashboard.truck;

import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.util.Link;

/**
 * @author aviolette
 * @since 2/9/17
 */
@Singleton
public class DangerZoneServlet extends AbstractTruckServlet {

  @Inject
  public DangerZoneServlet(TruckDAO truckDAO) {
    super(truckDAO);
  }

  @Override
  protected ImmutableList<Link> breadcrumbs(Truck truck) {
    return ImmutableList.of(new Link("Trucks", "/admin/trucks"),
        new Link(truck.getName(), "/admin/trucks/" + truck.getId()),
        new Link("Beacons", "/admin/trucks/" + truck.getId() + "/danger"));
  }

  @Override
  protected String getJsp() {
    return "/WEB-INF/jsp/dashboard/truck/dangerZone.jsp";
  }
}
