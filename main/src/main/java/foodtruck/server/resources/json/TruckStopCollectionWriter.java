package foodtruck.server.resources.json;

import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

import foodtruck.model.TruckStop;

/**
 * @author aviolette
 * @since 3/30/15
 */
@Provider
public class TruckStopCollectionWriter extends CollectionWriter<TruckStop, TruckStopWriter> {
  @Inject
  public TruckStopCollectionWriter(TruckStopWriter writer) {
    super(writer, TruckStop.class);
  }
}
