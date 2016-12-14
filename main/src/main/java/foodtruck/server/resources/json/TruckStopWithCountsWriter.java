package foodtruck.server.resources.json;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

import com.google.common.base.Joiner;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.TruckStopWithCounts;

/**
 * @author aviolette
 * @since 4/8/15
 */
@Provider
@Produces("application/json")
public class TruckStopWithCountsWriter implements JSONWriter<TruckStopWithCounts> {
  private TruckStopWriter writer;

  @Inject
  public TruckStopWithCountsWriter(TruckStopWriter writer) {
    this.writer = writer;
  }

  @Override
  public JSONObject asJSON(TruckStopWithCounts truckStopWithCounts) throws JSONException {
    JSONObject obj = writer.asJSON(truckStopWithCounts.getStop());
    obj.put("totalTruckCount", truckStopWithCounts.getTruckNames()
        .size());
    obj.put("truckNames", Joiner.on(", ")
        .join(truckStopWithCounts.getTruckNames()));
    return obj;
  }
}
