package foodtruck.server.job;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.json.jettison.JSONSerializer;
import foodtruck.model.TempTruckStop;
import foodtruck.net.UrlResource;
import foodtruck.schedule.ICalStopReader;

/**
 * @author aviolette
 * @since 2019-01-16
 */
@Singleton
public class SeedPizzaBossCalendarServlet extends AbstractJobServlet {

  private static final Logger log = Logger.getLogger(SeedPizzaBossCalendarServlet.class.getName());

  private final TempTruckStopDAO tempDAO;
  private final ICalStopReader reader;
  private final UrlResource urls;

  @Inject
  public SeedPizzaBossCalendarServlet(TempTruckStopDAO tempDAO, UrlResource urlResource,
      ICalStopReader reader) {
    this.tempDAO = tempDAO;
    this.reader = reader;
    this.urls = urlResource;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    for (int i=0; i < 3; i++) {
      List<TempTruckStop> stops = new LinkedList<>();
      try {
        JSONArray arr = urls.getAsArray(
            "https://us-central1-chicagofoodtrucklocator.cloudfunctions.net/chicagopizzaboss-schedule");
        for (String link : JSONSerializer.toStringList(arr)) {
          log.log(Level.INFO, "Loading link {0}", link);
          try {
            stops.addAll(reader.findStops(urls.getAsString(link), "chipizzaboss"));
          } catch (Exception e) {
            log.warning(e.getMessage());
          }
        }
        stops.forEach(tempDAO::save);
        resp.setStatus(200);
        break;
      } catch (JSONException e) {
        log.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
}
