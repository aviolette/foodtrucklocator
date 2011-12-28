package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.LocalTime;

import foodtruck.dao.TruckStopDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.TruckStop;
import foodtruck.model.Trucks;
import foodtruck.util.Clock;

/**
 * This should be a normal end-point.  I added this so I could seed the data when I am offline.
 * @author aviolette@gmail.com
 * @since 12/28/11
 */
@Singleton
public class SeedSampleData extends HttpServlet {
  private final TruckStopDAO truckStopDAO;
  private final Trucks trucks;
  private final Clock clock;
  private final GeoLocator locator;

  @Inject
  public SeedSampleData(TruckStopDAO truckStopDAO, Trucks trucks, Clock clock, GeoLocator locator) {
    this.truckStopDAO = truckStopDAO;
    this.trucks = trucks;
    this.clock = clock;
    this.locator = locator;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    TruckStop stop1 = new TruckStop(trucks.findById("ducknrolltruck"),
        clock.currentDay().toDateTime(new LocalTime(11, 30)),
        clock.currentDay().toDateTime(new LocalTime(13, 30)),
        locator.locate("Clinton and Lake, Chicago, IL",
            GeolocationGranularity.BROAD), null);
    truckStopDAO.addStops(ImmutableList.of(stop1));
  }
}
