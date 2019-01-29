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

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.net.UrlResource;
import foodtruck.schedule.ICalStopReader;
import foodtruck.schedule.SquarespaceLinkExtractor;

/**
 * @author aviolette
 * @since 2019-01-28
 */
@Singleton
public class SeedBigWangsServlet extends AbstractJobServlet {

  private static final Logger log = Logger.getLogger(SeedBigWangsServlet.class.getName());

  private static final String BASE_URL = "https://www.bigwangsfoodtruck.com/events";
  private final TempTruckStopDAO tempDAO;
  private final SquarespaceLinkExtractor extractor;
  private final ICalStopReader reader;
  private final UrlResource urls;

  @Inject
  public SeedBigWangsServlet(TempTruckStopDAO tempDAO, UrlResource urls,
      SquarespaceLinkExtractor extractor, ICalStopReader reader) {
    this.tempDAO = tempDAO;
    this.extractor = extractor;
    this.reader = reader;
    this.urls = urls;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    List<TempTruckStop> stops = new LinkedList<>();
    for (String link : extractor.findLinks(urls.getAsString("https://www.bigwangsfoodtruck.com/events"), BASE_URL)) {
      stops.addAll(reader.findStops(urls.getAsString(link), "bigwangschicago"));
    }
    stops.forEach(tempDAO::save);
    log.log(Level.INFO, "Loaded {0} stops", stops);
  }
}
