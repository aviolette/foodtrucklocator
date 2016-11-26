package foodtruck.server.dashboard.truck;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.util.Link;

/**
 * @author aviolette
 * @since 11/24/16
 */
@Singleton
public class TruckStatsServlet extends AbstractTruckServlet {
  private static final String JSP = "/WEB-INF/jsp/dashboard/truckStats.jsp";

  @Inject
  public TruckStatsServlet(TruckDAO truckDAO) {
    super(truckDAO);
  }

  @Override
  protected ImmutableList<Link> breadcrumbs(Truck truck) {
    return ImmutableList.of(new Link("Trucks", "/admin/trucks"),
        new Link(truck.getName(), "/admin/trucks/" + truck.getId()),
        new Link("Stats", "/admin/trucks/" + truck.getId() + "/stats"));
  }

  @Override
  protected String getJsp() {
    return JSP;
  }
}
