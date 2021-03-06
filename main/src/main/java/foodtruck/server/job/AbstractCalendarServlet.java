package foodtruck.server.job;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.model.TempTruckStop;

/**
 * @author aviolette
 * @since 2018-12-26
 */
public abstract class AbstractCalendarServlet extends AbstractJobServlet {

  private static final Logger log = Logger.getLogger(AbstractCalendarServlet.class.getName());

  private final TempTruckStopDAO tempDAO;

  AbstractCalendarServlet(TempTruckStopDAO dao) {
    this.tempDAO = dao;
  }

  @Override
  protected final void doPost(HttpServletRequest req, HttpServletResponse resp) {
    String calendar = req.getParameter("calendar");
    String truck = req.getParameter("truck");
    if (Strings.isNullOrEmpty(calendar)) {
      log.severe("Calendar is empty: " + truck);
      return;
    }
    log.log(Level.INFO, "Searching calendar {0} for truck {1}", new Object[] {calendar, truck});
    List<TempTruckStop> stops = doSearch(calendar, truck, req.getParameter("defaultLocation"));
    stops.forEach(tempDAO::save);
    log.log(Level.INFO, "Retrieved {0} stops for truck {2} with calendar: {1}", new Object[] {stops.size(), calendar, truck});
    resp.setStatus(200);
  }

  protected abstract List<TempTruckStop> doSearch(String calendar, String truck, String defaultLocation);
}
