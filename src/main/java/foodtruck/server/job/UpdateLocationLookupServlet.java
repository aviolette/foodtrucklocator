package foodtruck.server.job;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;

/**
 *
 * @author aviolette
 * @since 3/11/13
 */
@Singleton
public class UpdateLocationLookupServlet extends HttpServlet {
  private final LocationDAO dao;
  private static final Logger log = Logger.getLogger(UpdateLocationLookupServlet.class.getName());

  @Inject
  public UpdateLocationLookupServlet(LocationDAO dao) {
    this.dao = dao;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    log.info("Starting location migration");
    Collection<Location> locations = dao.findAll();
    for (Location loc : dao.findAll()) {
      dao.findByAddress(loc.getName());
      dao.save(loc);
    }
    log.info("Finished location migration");
  }
}
