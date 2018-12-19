package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import com.google.appengine.api.utils.SystemProperty;
import com.sun.jersey.api.client.Client;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.schedule.StopReader;

/**
 * @author aviolette
 * @since 2018-12-18
 */
public abstract class AbstractSeedServlet extends HttpServlet {

  private final TempTruckStopDAO tempDAO;
  private final Client client;
  private final StopReader reader;
  private final String endpoint;
  private final String userAgent;

  protected AbstractSeedServlet(TempTruckStopDAO tempDAO, Client client, StopReader reader, String endpoint,
      String userAgent) {
    this.tempDAO = tempDAO;
    this.client = client;
    this.reader = reader;
    this.endpoint = endpoint;
    this.userAgent = userAgent;
  }

  @Override
  protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (SystemProperty.environment.value() != SystemProperty.Environment.Value.Production) {
      doPost(req, resp);
    } else {
      super.doGet(req, resp);
    }
  }

  @Override
  protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String document = client.resource(endpoint)
        .header(HttpHeaders.USER_AGENT, userAgent)
        .get(String.class);
    reader.findStops(document).forEach(tempDAO::save);
    resp.setStatus(200);
  }
}
