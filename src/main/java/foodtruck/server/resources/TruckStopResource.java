package foodtruck.server.resources;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.joda.time.DateTime;

import foodtruck.model.TruckStop;
import foodtruck.model.TruckStopWithCounts;
import foodtruck.server.security.SecurityChecker;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.Session;


/**
 * @author aviolette
 * @since 3/30/15
 */
@Path("/v2/stops")
@Produces("application/json")
public class TruckStopResource {
  private static final Logger log = Logger.getLogger(TruckStopResource.class.getName());

  private final FoodTruckStopService foodTruckService;
  private final Clock clock;
  private final SecurityChecker checker;
  private final Provider<UserService> userServiceProvider;
  private final Provider<Session> sessionProvider;

  @Inject
  public TruckStopResource(FoodTruckStopService service, Clock clock, SecurityChecker checker,
      Provider<UserService> userServiceProvider, Provider<Session> sessionProvider) {
    this.foodTruckService = service;
    this.clock = clock;
    this.checker = checker;
    this.userServiceProvider = userServiceProvider;
    this.sessionProvider = sessionProvider;
  }

  @DELETE
  @Path("{stopId: \\d+}")
  public void delete(@PathParam("stopId") final long stopId)
      throws ServletException, IOException {
    if (!checker.isAdmin()) {
      TruckStop stop = foodTruckService.findById(stopId);
      if (stop == null) {
        return;
      }
      checker.requiresLoggedInAs(stop.getTruck().getId());
    }
    foodTruckService.delete(stopId);
  }

  @PUT
  public void save(TruckStop truckStop)
      throws ServletException, IOException {
    checker.requiresLoggedInAs(truckStop.getTruck().getId());
    log.log(Level.INFO, "Saving stop: {0}", truckStop);
    CapabilitiesService service =
        CapabilitiesServiceFactory.getCapabilitiesService();
    CapabilityStatus status = service.getStatus(Capability.DATASTORE_WRITE).getStatus();
    log.log(Level.INFO, "Data store: {0}", status);
    UserService userService = userServiceProvider.get();
    Principal principal = (Principal) sessionProvider.get().getProperty("principal");
    String whom = (principal != null) ? principal.getName() : userService.getCurrentUser().getEmail();
    foodTruckService.update(truckStop, whom);
  }

  @GET
  public Collection<TruckStopWithCounts> getStops(@QueryParam("truck") String truckId, @Context DateTime startTime) {
    startTime = (startTime == null) ? clock.currentDay().toDateMidnight().toDateTime() : startTime;
    return foodTruckService.findStopsForTruckAfter(truckId, startTime);
  }
}
