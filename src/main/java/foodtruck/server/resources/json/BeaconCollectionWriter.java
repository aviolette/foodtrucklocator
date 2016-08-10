package foodtruck.server.resources.json;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

import foodtruck.model.TrackingDevice;

/**
 * @author aviolette
 * @since 8/9/16
 */
@Provider
@Produces("application/json")
public class BeaconCollectionWriter extends CollectionWriter<TrackingDevice, BeaconWriter> {

  @Inject
  public BeaconCollectionWriter(BeaconWriter writer) {
    super(writer, TrackingDevice.class);
  }
}
