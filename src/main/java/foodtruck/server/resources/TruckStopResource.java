package foodtruck.server.resources;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import org.joda.time.DateTime;

import foodtruck.model.TruckLocationGroup;
import static foodtruck.server.resources.Resources.noCache;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 */
@Path("/stops")
@Produces("application/json")
public class TruckStopResource {
  private FoodTruckStopService foodTruckService;
  private Clock clock;

  @Inject
  public TruckStopResource(FoodTruckStopService service, Clock clock) {
    this.foodTruckService = service;
    this.clock = clock;
  }

  @GET
  public JResponse<Set<TruckLocationGroup>> getStops() {
    DateTime requestTime = clock.now();
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