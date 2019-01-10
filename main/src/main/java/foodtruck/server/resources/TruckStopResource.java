package foodtruck.server.resources;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.google.api.client.util.Strings;
import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.users.UserService;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.joda.time.DateTime;

import foodtruck.annotations.AppKey;
import foodtruck.annotations.RequiresAppKeyWithCountRestriction;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.TruckStop;
import foodtruck.model.TruckStopWithCounts;
import foodtruck.monitoring.Monitored;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.server.security.SecurityChecker;
import foodtruck.session.Session;
import foodtruck.time.Clock;

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
  private final TruckStopDAO truckStopDAO;

  @Inject
  public TruckStopResource(FoodTruckStopService service, Clock clock, SecurityChecker checker,
      Provider<UserService> userServiceProvider, Provider<Session> sessionProvider,
      TruckStopDAO truckStopDAO) {
    this.foodTruckService = service;
    this.clock = clock;
    this.checker = checker;
    this.userServiceProvider = userServiceProvider;
    this.sessionProvider = sessionProvider;
    this.truckStopDAO = truckStopDAO;
  }

  @DELETE
  @Path("{stopId: \\d+}")
  @Monitored
  public void delete(@PathParam("stopId") final long stopId) throws ServletException, IOException {
    if (!checker.isAdmin()) {
      Optional<TruckStop> stop = truckStopDAO.findByIdOpt(stopId);
      if (!stop.isPresent()) {
        return;
      }
      checker.requiresLoggedInAs(stop.get().getTruck().getId());
    }
    foodTruckService.delete(stopId);
  }

  @PUT
  @Monitored
  public void save(TruckStop truckStop) throws ServletException, IOException {
    checker.requiresLoggedInAs(truckStop.getTruck()
        .getId());
    log.log(Level.INFO, "Saving stop: {0}", truckStop);
    CapabilitiesService service = CapabilitiesServiceFactory.getCapabilitiesService();
    CapabilityStatus status = service.getStatus(Capability.DATASTORE_WRITE)
        .getStatus();
    log.log(Level.INFO, "Data store: {0}", status);
    UserService userService = userServiceProvider.get();
    Principal principal = (Principal) sessionProvider.get()
        .getProperty("principal");
    String whom = (principal != null) ? principal.getName() : userService.getCurrentUser()
        .getEmail();
    foodTruckService.update(truckStop, whom);
  }

  // TODO: should require login
  @GET
  @Monitored
  public List<TruckStopWithCounts> getStops(@QueryParam("truck") String truckId,
      @Context DateTime startTime, @QueryParam("includeCounts") boolean includeCounts,
      @AppKey @QueryParam("appKey") final String appKey) {
    if (Strings.isNullOrEmpty(truckId)) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    startTime = (startTime == null) ? clock.currentDay().toDateTimeAtStartOfDay() : startTime;
    if (includeCounts) {
      return foodTruckService.findStopsForTruckAfter(truckId, startTime);
    } else {
      return foodTruckService.findStopsForTruckAfterWithoutCounts(truckId, startTime)
          .stream()
          .map(truckStop -> new TruckStopWithCounts(truckStop, ImmutableSet.of()))
          .collect(Collectors.toList());
    }
  }

  @GET
  @Path("{truckId}")
  @Monitored
  @RequiresAppKeyWithCountRestriction
  public List<TruckStopWithCounts> getStop(@PathParam("truckId") String truckId,
      @AppKey @QueryParam("appKey") String appKey, @Context DateTime startTime,
      @QueryParam("includeCounts") boolean includeStops) {
    startTime = (startTime == null) ? clock.currentDay()
        .toDateTimeAtStartOfDay() : startTime;
    if (includeStops) {
      return foodTruckService.findStopsForTruckAfter(truckId, startTime);
    } else {
      return foodTruckService.findStopsForTruckAfterWithoutCounts(truckId, startTime)
          .stream()
          .map(truckStop -> new TruckStopWithCounts(truckStop, ImmutableSet.of()))
          .collect(Collectors.toList());
    }
  }
}
