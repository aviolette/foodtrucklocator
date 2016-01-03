package foodtruck.server.resources.json;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.DailyData;

/**
 * @author aviolette
 * @since 12/16/15
 */
public class DailyDataWriter implements JSONWriter<DailyData> {
  @Override
  public JSONObject asJSON(DailyData dailyData) throws JSONException {
    return new JSONObject()
        .put("truckId", dailyData.getTruckId())
        .put("specials", FluentIterable.from(dailyData.getSpecials()).transform(new Function<DailyData.SpecialInfo, String>() {
          public String apply(DailyData.SpecialInfo input) {
            return input.getSpecial();
          }
        }).toList());
  }
}
