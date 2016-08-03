package foodtruck.server.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;

import com.google.inject.Inject;

import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.model.TrackingDevice;

/**
 * @author aviolette
 * @since 8/2/16
 */
@Path("/beacons")
public class BeaconResource {
  private final TrackingDeviceDAO trackingDeviceDAO;

  @Inject
  public BeaconResource(TrackingDeviceDAO trackDeviceDAO) {
    this.trackingDeviceDAO = trackDeviceDAO;
  }

  @POST
  @Path("{beaconId}/enable")
  public void enable(@PathParam("beaconId") Long beaconId) {
    enable(beaconId, true);
  }

  @POST
  @Path("{beaconId}/disable")
  public void disable(@PathParam("beaconId") Long beaconId) {
    enable(beaconId, false);
  }

  private void enable(Long beaconId, boolean enabled) {
    TrackingDevice device = trackingDeviceDAO.findById(beaconId);
    if (device == null) {
      throw new WebApplicationException(404);
    }
    trackingDeviceDAO.save(TrackingDevice.builder(device).enabled(enabled).build());
  }

}