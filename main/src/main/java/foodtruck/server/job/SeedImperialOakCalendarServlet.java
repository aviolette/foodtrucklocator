package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.utils.SystemProperty;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.net.UrlResource;
import foodtruck.schedule.SimpleCalReader;

/**
 * @author aviolette
 * @since 2018-12-11
 */
@Singleton
public class SeedImperialOakCalendarServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(SeedImperialOakCalendarServlet.class.getName());

  private final TempTruckStopDAO tempDAO;
  private final SimpleCalReader reader;
  private final UrlResource urls;

  @Inject
  public SeedImperialOakCalendarServlet(TempTruckStopDAO tempDAO, SimpleCalReader reader, UrlResource urls) {
    this.tempDAO = tempDAO;
    this.reader = reader;
    this.urls = urls;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (SystemProperty.environment.value() != SystemProperty.Environment.Value.Production) {
      doPost(req, resp);
    } else {
      super.doGet(req, resp);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    log.log(Level.INFO, "Updating Imperial Oaks calendar data");
    JSONArray arr = urls.getAsArray("https://us-central1-chicagofoodtrucklocator.cloudfunctions.net/imperial-oak");
    try {
      for (TempTruckStop stop : reader.read(arr, "imperialoak" , "Imperial Oak Brewery")) {
        tempDAO.save(stop);
      }
    } catch (JSONException e) {
      throw new ServletException(e);
    }
    if (!Strings.isNullOrEmpty(req.getParameter("redirect"))) {
      resp.sendRedirect(req.getParameter("redirect"));
    }
    log.log(Level.INFO, "Imperial Oaks data completed");
  }
}
