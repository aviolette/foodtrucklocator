package foodtruck.server.resources.json;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.StatVector;
import foodtruck.model.TimeValue;

/**
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
public class StatVectorWriter implements JSONWriter<StatVector> {
  @Override
  public JSONObject asJSON(StatVector statVector) throws JSONException {
    JSONArray dataArray = toDataArray(statVector.getDataPoints());
    return new JSONObject().put("name", statVector.getName())
        .put("data", dataArray);
  }

  private JSONArray toDataArray(List<TimeValue> dataPoints) throws JSONException {
    JSONArray arr = new JSONArray();
    for (TimeValue dataPoint : dataPoints) {
      arr.put(new JSONObject().put("x", dataPoint.getTimestamp() / 1000)
          .put("y", dataPoint.getCount()));
    }
    return arr;
  }
}
