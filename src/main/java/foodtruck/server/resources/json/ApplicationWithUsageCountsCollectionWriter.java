package foodtruck.server.resources.json;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

import foodtruck.model.ApplicationWithUsageCounts;

/**
 * @author aviolette
 * @since 6/13/15
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationWithUsageCountsCollectionWriter extends CollectionWriter<ApplicationWithUsageCounts, ApplicationWithUsageCountsWriter> {

  @Inject
  public ApplicationWithUsageCountsCollectionWriter(ApplicationWithUsageCountsWriter writer) {
    super(writer, ApplicationWithUsageCounts.class);
  }
}
