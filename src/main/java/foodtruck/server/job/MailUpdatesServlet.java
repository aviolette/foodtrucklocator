package foodtruck.server.job;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckStopChangeDAO;
import foodtruck.model.TruckStopChange;

/**
 * @author aviolette@gmail.com
 * @since 5/31/12
 */

@Singleton
public class MailUpdatesServlet extends HttpServlet {
  private final TruckStopChangeDAO truckStopChangeDAO;

  @Inject
  public MailUpdatesServlet(TruckStopChangeDAO truckStopChangeDAO) {
    this.truckStopChangeDAO = truckStopChangeDAO;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // TODO: fix this logic, right now it just deletes the changes
    Collection<TruckStopChange> changes = truckStopChangeDAO.findAll();
    truckStopChangeDAO.deleteAll(changes);
  }
}
