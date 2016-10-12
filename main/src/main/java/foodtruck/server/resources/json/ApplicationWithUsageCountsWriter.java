package foodtruck.server.resources.json;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.ApplicationWithUsageCounts;

/**
 * @author aviolette
 * @since 6/13/15
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationWithUsageCountsWriter implements JSONWriter<ApplicationWithUsageCounts> {
  private final ApplicationWriter applicationWriter;

  @Inject
  public ApplicationWithUsageCountsWriter(ApplicationWriter applicationwriter) {
    this.applicationWriter = applicationwriter;
  }

  @Override
  public JSONObject asJSON(ApplicationWithUsageCounts applicationWithUsageCounts) throws JSONException {
    JSONObject obj = applicationWriter.asJSON(applicationWithUsageCounts.getApplication());
    obj.put("dailyCounts", applicationWithUsageCounts.getDailyCount());
    return obj;
  }
}
