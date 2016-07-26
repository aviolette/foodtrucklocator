package foodtruck.server.job;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.linxup.LinxupConnector;
import foodtruck.linxup.Position;
import foodtruck.truckstops.FoodTruckStopService;


/**
 * @author aviolette
 * @since 7/21/16
 */
@Singleton
public class TruckMonitorServlet extends HttpServlet {
  private final FoodTruckStopService stopService;
  private final LinxupConnector connector;

  @Inject
  public TruckMonitorServlet(FoodTruckStopService service, LinxupConnector connector) {
    this.stopService = service;
    this.connector = connector;

  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    List<Position> positionList = connector.findPositions();
  }
}
