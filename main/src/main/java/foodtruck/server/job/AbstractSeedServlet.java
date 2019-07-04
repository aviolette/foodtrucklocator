package foodtruck.server.job;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Provider;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.net.UrlResource;
import foodtruck.schedule.StopReader;

/**
 * @author aviolette
 * @since 2018-12-18
 */
public abstract class AbstractSeedServlet extends AbstractJobServlet {

  private static final Logger log = Logger.getLogger(AbstractSeedServlet.class.getName());

  private final TempTruckStopDAO tempDAO;
  private final StopReader reader;
  private final boolean retryOnceBeforeError;
  private final UrlResource urls;
  private final Provider<String> endpointProvider;

  @SuppressWarnings("WeakerAccess")
  protected AbstractSeedServlet(TempTruckStopDAO tempDAO, StopReader reader, boolean retryOnceBeforeError, UrlResource urls, Provider<String> endpointProvider) {
    this.tempDAO = tempDAO;
    this.reader = reader;
    this.retryOnceBeforeError = retryOnceBeforeError;
    this.urls = urls;
    this.endpointProvider = endpointProvider;
  }

  @Override
  protected final void doPost(HttpServletRequest req, HttpServletResponse resp) {
    String endpoint = endpointProvider.get();
    try {
      String document = urls.getAsString(endpoint);
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
