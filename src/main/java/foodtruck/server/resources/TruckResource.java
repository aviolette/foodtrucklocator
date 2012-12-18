package foodtruck.server.resources;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import org.joda.time.DateTimeZone;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.util.Clock;
import static foodtruck.server.resources.Resources.requiresAdmin;

/**
 * @author aviolette@gmail.com
 * @since 6/13/12
 */
@Path("/trucks{view : (\\.[a-z]{3})?}")
public class TruckResource {
  private final TruckDAO truckDAO;
  private final Clock clock;
  private final DateTimeZone zone;

  @Inject
  public TruckResource(TruckDAO truckDAO, Clock clock, DateTimeZone zone) {
    this.truckDAO = truckDAO;
    this.clock = clock;
    this.zone = zone;
  }

  @GET
  @Produces({"application/json", "text/csv"})
  public JResponse<Collection<Truck>> getTrucks(@PathParam("view") String view) {
    MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
    if (".csv".equals(view)) {
      mediaType = new MediaType("text", "csv");
    }
    return JResponse.ok(truckDAO.findActiveTrucks(), mediaType).build();
  }

  @POST @Path("{truckId}/mute")
  public void muteTruck(@PathParam("truckId") String truckId) {
    requiresAdmin();
    Truck t = truckDAO.findById(truckId);
    t = Truck.builder(t).muteUntil(clock.currentDay().toDateMidnight(zone).toDateTime().plusDays(1))
        .build();
    truckDAO.save(t);
  }

  @POST @Path("{truckId}/unmute")
  public void unmuteTruck(@PathParam("truckId") String truckId) {
    requiresAdmin();
    Truck t = truckDAO.findById(truckId);
    t = Truck.builder(t).muteUntil(null)
        .build();
    truckDAO.save(t);
  }

  @DELETE @Path("{truckId}")
  public void delete(@PathParam("truckId") String truckId) {
    truckDAO.delete(truckId);
  }

  @POST @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
  public JResponse<Truck> createTruck(Truck truck) {
    Resources.requiresAdmin();
    if (truckDAO.findById(truck.getId()) != null) {
      throw new BadRequestException("POST can only be used for creating objects");
    }

    truckDAO.save(truck);
    return JResponse.ok(truck).build();
  }
}
