package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.mail.SystemNotificationService;
import foodtruck.time.Clock;

/**
 * A servlet
 * @author aviolette
 * @since 4/29/13
 */
@Singleton
public class TestNotificationServlet extends HttpServlet {
  private final SystemNotificationService notifier;
  private final TruckDAO truckDAO;
  private final Clock clock;

  @Inject
  public TestNotificationServlet(SystemNotificationService notifier, TruckDAO truckDAO, Clock clock) {
    this.notifier = notifier;
    this.truckDAO = truckDAO;
    this.clock = clock;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/testNotification.jsp").forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Truck truck = Iterables.getFirst(truckDAO.findAll(), null);
    Story tweet = Story.builder().
        text("We are off the road today.").time(clock.now()).build();
    notifier.systemNotifyOffTheRoad(truck, tweet);
    resp.sendRedirect("/admin");
  }
}
