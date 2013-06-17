package foodtruck.server.resources.json;

import javax.ws.rs.ext.Provider;

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
  public TruckObserverCollectionWriter(TruckObserverWriter writer) {
    super(writer, TruckObserver.class);
  }
}
