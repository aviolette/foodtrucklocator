package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.linxup.TruckMonitorService;

/**
 * Called periodically to query for updates on the beacon.
 *
 * @author aviolette
 * @since 7/21/16
 */
@Singleton
public class TruckMonitorServlet extends HttpServlet {
  private final TruckMonitorService service;

  @Inject
  public TruckMonitorServlet(TruckMonitorService service) {
    this.service = service;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    this.service.synchronize();
  }
}
