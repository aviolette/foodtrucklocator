package foodtruck.server.dashboard.truck;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.Link;

/**
 * @author aviolette
 * @since 11/26/16
 */
@Singleton
public class OffTheRoadServlet extends AbstractTruckServlet {
  private final FoodTruckStopService truckService;
  private final Clock clock;

  @Inject
  public OffTheRoadServlet(TruckDAO truckDAO, FoodTruckStopService truckService, Clock clock) {
    super(truckDAO);
    this.truckService = truckService;
    this.clock = clock;
  }

  @Override
  protected void doPostProtected(HttpServletRequest request, HttpServletResponse response,
      Truck truck) throws IOException {
    truckService.offRoad(truck.getId(), clock.currentDay());
    response.sendRedirect("/admin/trucks/" + truck.getId());
  }

  @Override
  protected ImmutableList<Link> breadcrumbs(Truck truck) {
    return ImmutableList.of(new Link("Trucks", "/admin/trucks"),
        new Link(truck.getName(), "/admin/trucks/" + truck.getId()),
        new Link("Beacons", "/admin/trucks/" + truck.getId() + "/offtheroad"));
  }

  @Override
  protected String getJsp() {
    return "/WEB-INF/jsp/dashboard/offTheRoad.jsp";
  }
}
