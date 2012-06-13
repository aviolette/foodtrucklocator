package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.config.TruckConfigParser;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.model.Trucks;

/**
 * Endpoint used to sync
 * @author aviolette@gmail.com
 * @since 2/27/12
 */
@Singleton
public class SyncTrucksServlet extends HttpServlet {
  private final TruckDAO truckDAO;
  private TruckConfigParser parser;

  @Inject
  public SyncTrucksServlet(TruckConfigParser parser, TruckDAO truckDAO) {
    this.parser = parser;
    this.truckDAO = truckDAO;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String url =
        Thread.currentThread().getContextClassLoader().getResource("trucks.yaml").getFile();
    Trucks trucks = parser.parse(url);

    for (Truck truck : trucks.allTrucks()) {
      if (truckDAO.findById(truck.getId()) == null) {
        truckDAO.save(truck);
      }
    }
  }
}
