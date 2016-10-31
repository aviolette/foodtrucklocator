package foodtruck.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;

import foodtruck.linxup.TruckMonitorService;

/**
 * @author aviolette
 * @since 8/2/16
 */
@Path("/beacons")
public class BeaconResource {
  private final TruckMonitorService service;

  @Inject
  public BeaconResource(TruckMonitorService service) {
    this.service = service;
  }

  @GET
  @Path("{beaconId}/trips")
  public Response getRecentTripList(@PathParam("beaconId") Long beaconId) {
    // TODO: make this legit
    String response = service.getRecentTripList(beaconId);
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE)
        .build();
  }

  @POST
  @Path("{beaconId}/enable")
  public void enable(@PathParam("beaconId") Long beaconId) {
    service.enableDevice(beaconId, true);
  }

  @POST
  @Path("{beaconId}/disable")
  public void disable(@PathParam("beaconId") Long beaconId) {
    service.enableDevice(beaconId, false);
  }
}