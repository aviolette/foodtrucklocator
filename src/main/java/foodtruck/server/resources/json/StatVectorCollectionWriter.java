package foodtruck.server.resources.json;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

import foodtruck.model.StatVector;

/**
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
@Provider @Produces(MediaType.APPLICATION_JSON)
public class StatVectorCollectionWriter extends CollectionWriter<StatVector, StatVectorWriter> {
  /**
   * Constructs the collection writer
   * @param writer the writer used to output each entity
   */
  @Inject
  public StatVectorCollectionWriter(StatVectorWriter writer) {
    super(writer, StatVector.class);
  }
}
