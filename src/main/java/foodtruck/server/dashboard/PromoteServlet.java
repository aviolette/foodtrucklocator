package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.FoodTruckRequestDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.email.EmailNotifier;
import foodtruck.model.FoodTruckRequest;
import foodtruck.model.Truck;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 2/23/14
 */
@Singleton
public class PromoteServlet extends HttpServlet {
  private final EmailNotifier notifier;
  private final TruckDAO truckDAO;
  private final FoodTruckRequestDAO requestDAO;
  private final Clock clock;
  private static final Logger log = Logger.getLogger(PromoteServlet.class.getName());

  @Inject
  public PromoteServlet(EmailNotifier notifier, TruckDAO truckDAO, FoodTruckRequestDAO requestDAO, Clock clock) {
    this.notifier = notifier;
    this.truckDAO = truckDAO;
    this.requestDAO = requestDAO;
    this.clock = clock;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String id = req.getParameter("id");
    FoodTruckRequest request = requestDAO.findById(Long.parseLong(id));
    if (request == null || request.isArchived()) {
      resp.setStatus(404);
      return;
    }
    if (request.getApproved() != null && !"true".equals(req.getParameter("override"))) {
      log.log(Level.INFO, "Request {0} not mailed 'cause it has already been promoted", id);
      return;
    }
    boolean success = notifier.notifyFoodTrucksOfRequest(FluentIterable.from(truckDAO.findTrucksWithEmail())
        .transform(new Function<Truck, String>() {
          @Nullable @Override public String apply(@Nullable Truck truck) {
            return truck.getEmail();
          }
        }), request);
    if (success) {
      request = FoodTruckRequest.builder(request).approved(clock.now()).build();
      requestDAO.save(request);
    }
  }
}
