package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.linxup.TrackingDeviceService;

/**
 * Called periodically to query for updates on the beacon.
 *
 * @author aviolette
 * @since 7/21/16
 */
@Singleton
public class TruckMonitorServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(TruckMonitorServlet.class.getName());
  private final TrackingDeviceService service;

  @Inject
  public TruckMonitorServlet(TrackingDeviceService service) {
    this.service = service;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    log.info("Synchronizing monitoring devices...");
    this.service.synchronize();
    String refreshValue = req.getParameter("refresh");
    if (Strings.isNullOrEmpty(refreshValue)) {
      return;
    }
    resp.setHeader("Content-Type", "text/html");
    resp.getOutputStream()
        .println("<html><head><meta http-equiv=\"refresh\" content=\"" + refreshValue + "\"></head></html>");
  }
}
