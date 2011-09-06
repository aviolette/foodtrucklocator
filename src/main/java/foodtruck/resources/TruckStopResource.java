package foodtruck.resources;

import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import foodtruck.model.TruckLocationGroup;
import foodtruck.truckstops.FoodTruckStopService;

/**
 * @author aviolette@gmail.com
 * @since 9/2/11
 */
@Path("/stops")
@Produces("application/json")
public class TruckStopResource {
  private final FoodTruckStopService foodTruckService;
  private final DateTimeZone zone;

  @Inject
  public TruckStopResource(FoodTruckStopService service, DateTimeZone zone) {
    this.foodTruckService = service;
    this.zone = zone;
  }

  @GET
  public List<TruckLocationGroup> findAll(@Nullable @Context DateTime dateTime) {
    if (dateTime == null) {
      dateTime = new DateTime(zone);
    }
    // jaxb - so annoying...doesn't like sets
    return ImmutableList.copyOf(foodTruckService.findFoodTruckGroups(dateTime));
  }
}
