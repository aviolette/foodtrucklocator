package foodtruck.server.resources.json;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
@Provider
@Produces("application/json")
public class TruckCollectionWriter extends CollectionWriter<Truck, TruckWriter> {
  @Inject
  public TruckCollectionWriter(TruckWriter writer, ObjectMapper objectMapper) {
    super(writer, Truck.class, objectMapper);
  }
}