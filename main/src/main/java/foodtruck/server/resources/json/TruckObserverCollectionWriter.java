package foodtruck.server.resources.json;

import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import foodtruck.model.TruckObserver;

/**
 * @author aviolette
 * @since 6/13/13
 */
@Provider
public class TruckObserverCollectionWriter extends CollectionWriter<TruckObserver, TruckObserverWriter> {
  /**
   * Constructs the collection writer
   * @param writer the writer used to output each entity
   */
  @Inject
  public TruckObserverCollectionWriter(TruckObserverWriter writer, ObjectMapper objectMapper) {
    super(writer, TruckObserver.class, objectMapper);
  }
}
