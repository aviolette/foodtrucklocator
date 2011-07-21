package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import foodtruck.truckstops.FoodTruckStopService;

/**
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
@Singleton
public class FoodTruckUpdaterServlet extends HttpServlet implements Runnable {
  private final FoodTruckStopService service;
  private final DateTimeZone currentZone;

  @Inject
  public FoodTruckUpdaterServlet(FoodTruckStopService service, DateTimeZone currentZone) {
    this.service = service;
    this.currentZone = currentZone;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    run();
  }

  @Override
  public void run() {
    service.updateStopsFor(new LocalDate(currentZone));  
  }
}
