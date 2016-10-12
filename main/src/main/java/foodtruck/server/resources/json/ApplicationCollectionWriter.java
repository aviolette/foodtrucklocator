package foodtruck.server.resources.json;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

import foodtruck.model.Application;

/**
 * @author aviolette
 * @since 1/25/13
 */
@Provider @Produces("application/json")
public class ApplicationCollectionWriter extends CollectionWriter<Application, ApplicationWriter> {
  /**
   * Constructs the collection writer
   * @param writer the writer used to output each entity
   */
  @Inject
  public ApplicationCollectionWriter(ApplicationWriter writer) {
    super(writer, Application.class);
  }
}
