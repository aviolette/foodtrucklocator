package foodtruck.server.resources.json;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import foodtruck.linxup.Trip;

/**
 * @author aviolette
 * @since 11/2/16
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class TripCollectionWriter extends CollectionWriter<Trip, TripWriter> {
  @Inject
  public TripCollectionWriter(TripWriter writer, ObjectMapper objectMapper) {
    super(writer, Trip.class, objectMapper);
  }
}
