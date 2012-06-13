package foodtruck.server.resources.json;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

import foodtruck.model.TruckLocationGroup;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
@Provider
@Produces("application/json")
public class TruckLocationGroupCollectionWriter
    extends CollectionWriter<TruckLocationGroup, TruckLocationGroupWriter> {
  @Inject
  public TruckLocationGroupCollectionWriter(TruckLocationGroupWriter writer) {
    super(writer, TruckLocationGroup.class);
  }
}