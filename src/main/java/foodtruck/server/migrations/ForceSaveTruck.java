package foodtruck.server.migrations;

import java.io.IOException;

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
 * @since 5/21/13
 */
@Singleton
public class ForceSaveTruck extends HttpServlet {
  private final TruckDAO dao;

  @Inject
  public ForceSaveTruck(TruckDAO dao) {
    this.dao = dao;
  }
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    for (Truck truck : dao.findAll()) {
      dao.save(truck);
    }
  }
}
