package foodtruck.server.resources;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import org.joda.time.DateTime;

import foodtruck.model.TruckLocationGroup;
import foodtruck.model.TruckStop;
import static foodtruck.server.resources.Resources.noCache;
import static foodtruck.server.resources.Resources.requiresAdmin;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 */
@Path("/stops")
@Produces("application/json")
public class TruckStopResource {
  private final FoodTruckStopService foodTruckService;
  private final Clock clock;

  @Inject
  public TruckStopResource(FoodTruckStopService service, Clock clock) {
    this.foodTruckService = service;
    this.clock = clock;
  }

  @DELETE @Path("{stopId: \\d+}")
  public void delete(@PathParam("stopId") final long stopId)
      throws ServletException, IOException {
    requiresAdmin();
    foodTruckService.delete(stopId);
  }

  @PUT
  public void save(TruckStop truckStop)
      throws ServletException, IOException {
    requiresAdmin();
    foodTruckService.update(truckStop);
  }

  @GET
  public JResponse<Set<TruckLocationGroup>> getStops(@Context DateTime requestTime) {
    requestTime = (requestTime == null) ? clock.now() : requestTime;
    Set<TruckLocationGroup> foodTruckGroups = foodTruckService.findFoodTruckGroups(requestTime);
    ImmutableSet.Builder<TruckLocationGroup> builder = ImmutableSet.builder();
    for (TruckLocationGroup group : foodTruckGroups) {
      if (group.getLocation() != null) {
        builder.add(group);
      }
    }
    return noCache(JResponse.ok((Set<TruckLocationGroup>) builder.build())).build();
  }
}