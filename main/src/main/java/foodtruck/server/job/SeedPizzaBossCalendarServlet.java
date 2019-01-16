package foodtruck.server.job;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.json.jettison.JSONSerializer;
import foodtruck.model.TempTruckStop;
import foodtruck.schedule.ICalStopReader;

/**
 * @author aviolette
 * @since 2019-01-16
 */
@Singleton
public class SeedPizzaBossCalendarServlet extends AbstractJobServlet {

  private static final Logger log = Logger.getLogger(SeedPizzaBossCalendarServlet.class.getName());

  private final String userAgent;
  private final TempTruckStopDAO tempDAO;
  private final Client client;
  private final ICalStopReader reader;

  @Inject
  public SeedPizzaBossCalendarServlet(TempTruckStopDAO tempDAO, Client client, @UserAgent String userAgent,
      ICalStopReader reader) {
    this.client = client;
    this.tempDAO = tempDAO;
    this.userAgent = userAgent;
    this.reader = reader;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    for (int i=0; i < 3; i++) {
      List<TempTruckStop> stops = new LinkedList<>();
      try {
        JSONArray arr = client
            .resource(
            "https://us-central1-chicagofoodtrucklocator.cloudfunctions.net/chicagopizzaboss-schedule")
            .header(HttpHeaders.USER_AGENT, userAgent)
            .get(JSONArray.class);
        for (String link : JSONSerializer.toStringList(arr)) {
          log.log(Level.INFO, "Loading link {0}", link);
          try {
            String iCalDocument = client.resource(link)
                .header(HttpHeaders.USER_AGENT, userAgent)
                .get(String.class);
            stops.addAll(reader.findStops(iCalDocument, "chipizzaboss"));
          } catch (Exception e) {
            log.warning(e.getMessage());
          }
        }
        stops.forEach(tempDAO::save);
        resp.setStatus(200);
      } catch (JSONException e) {
        log.log(Level.WARNING, e.getMessage(), e);
      }
    }
  }
}
