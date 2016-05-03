package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 3/12/13
 */
@Singleton
public class UpdateAllTrucks extends HttpServlet {
  private static final Logger log = Logger.getLogger(UpdateTruckStats.class.getName());
  private final TruckDAO truckDAO;

  @Inject
  public UpdateAllTrucks(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    for (Truck truck : truckDAO.findAll()) {
      truckDAO.save(truck);
    }
  }
}
