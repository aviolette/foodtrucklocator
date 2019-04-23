package foodtruck.server.job;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TempTruckStopDAO;
import foodtruck.json.jettison.JSONSerializer;
import foodtruck.model.TempTruckStop;
import foodtruck.schedule.LocationICalStopReader;

/**
 * @author aviolette
 * @since 2019-01-16
 */
@Singleton
public class SeedRoyalPalmsScheduleServlet extends AbstractJobServlet {

  private static final Logger log = Logger.getLogger(SeedRoyalPalmsScheduleServlet.class.getName());

  private final GcsService storageService;
  private final LocationICalStopReader stopReader;
  private final Client client;
  private final String userAgent;
  private final TempTruckStopDAO tempDAO;

  @Inject
  public SeedRoyalPalmsScheduleServlet(TempTruckStopDAO tempDAO, GcsService storageService,
      LocationICalStopReader stopReader, Client client, @UserAgent String userAgent) {
    this.storageService = storageService;
    this.stopReader = stopReader;
    this.client = client;
    this.userAgent = userAgent;
    this.tempDAO = tempDAO;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    GcsFilename fileName = new GcsFilename("cftf_schedule", "royalpalms.json");
    GcsInputChannel readChannel = storageService.openPrefetchingReadChannel(fileName, 0, 2097152);
    try (InputStream in = Channels.newInputStream(readChannel)) {
      String json = new String(ByteStreams.toByteArray(in), "UTF-8");
      JSONArray arr = new JSONArray(json);
      LinkedList<TempTruckStop> stops = new LinkedList<>();
      for (String link : JSONSerializer.toStringList(arr)) {
        log.log(Level.INFO, "Loading link {0}", link);
        try {
          String iCalDocument = client.resource(link)
              .header(HttpHeaders.USER_AGENT, userAgent)
              .get(String.class);
          stops.addAll(stopReader.findStops(iCalDocument, "Royal Palms", "Royal Palms"));
        } catch (Exception e) {
          log.warning(e.getMessage());
        }
      }
      stops.forEach(tempDAO::save);
      resp.setStatus(200);
    } catch (JSONException| FileNotFoundException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }
}