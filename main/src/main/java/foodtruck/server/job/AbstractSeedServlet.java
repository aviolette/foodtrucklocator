package foodtruck.server.job;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.schedule.StopReader;

/**
 * @author aviolette
 * @since 2018-12-18
 */
public abstract class AbstractSeedServlet extends AbstractJobServlet {

  private static final Logger log = Logger.getLogger(AbstractSeedServlet.class.getName());

  private final TempTruckStopDAO tempDAO;
  private final Client client;
  private final StopReader reader;
  private final String endpoint;
  private final String userAgent;
  private final boolean retryOnceBeforeError;

  @SuppressWarnings("WeakerAccess")
  protected AbstractSeedServlet(TempTruckStopDAO tempDAO, Client client, StopReader reader, String endpoint,
      String userAgent, boolean retryOnceBeforeError) {
    this.tempDAO = tempDAO;
    this.client = client;
    this.reader = reader;
    this.endpoint = endpoint;
    this.userAgent = userAgent;
    this.retryOnceBeforeError = retryOnceBeforeError;
  }

  @Override
  protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String document = client.resource(endpoint)
          .header(HttpHeaders.USER_AGENT, userAgent)
          .get(String.class);
      List<TempTruckStop> stops = reader.findStops(document);
      log.log(Level.INFO, "Retrieved {0} stops for calendar: {1}", new Object[]{stops.size(), getCalendar()});
      stops.forEach(tempDAO::save);
      resp.setStatus(200);
    } catch (RuntimeException e) {
      // I mainly added this because coastline cove seems to need to be primed before it can be retrieved
      String retryCountHeader = req.getHeader("X-AppEngine-TaskRetryCount");
      int retryCount;
      if (!Strings.isNullOrEmpty(retryCountHeader)) {
        retryCount = Integer.parseInt(retryCountHeader);
      } else {
        retryCount = 1;
      }
      if (!retryOnceBeforeError || retryCount > 0) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }

      if (retryOnceBeforeError && retryCount > 2) {
        log.log(Level.SEVERE, "Abandoning endpoint call: " + endpoint);
        resp.setStatus(200);
        return;
      }
      resp.setStatus(500);
    }
  }

  protected String getCalendar() {
    return reader.getCalendar();
  }
}
