package foodtruck.server.resources.json;

import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 4/30/14
 */
@Provider
public class LocationCollectionWriter extends CollectionWriter<Location, LocationWriter> {
  /**
   * Constructs the collection writer
   * @param writer the writer used to output each entity
   */
  @Inject
  public LocationCollectionWriter(LocationWriter writer) {
    super(writer, Location.class);
  }
}
