package foodtruck.server.resources.json;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.TruckLocationGroup;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
public class TruckLocationGroupWriter implements JSONWriter<TruckLocationGroup> {
  private final LocationWriter locationWriter;
  private final TruckWriter truckWriter;

  @Inject
  public TruckLocationGroupWriter(LocationWriter writer, TruckWriter truckWriter) {
    this.locationWriter = writer;
    this.truckWriter = truckWriter;
  }

  @Override public JSONObject asJSON(TruckLocationGroup group) throws JSONException {
    return new JSONObject()
        .putOpt("location", group.getLocation() == null ? null :
            locationWriter.asJSON(group.getLocation()))
        .put("trucks", JSONSerializer.buildArray(group.getTrucks(), truckWriter));
  }
}