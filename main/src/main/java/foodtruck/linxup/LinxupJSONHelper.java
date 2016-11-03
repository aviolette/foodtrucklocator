package foodtruck.linxup;

import com.javadocmd.simplelatlng.LatLng;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

/**
 * @author aviolette
 * @since 11/2/16
 */
public class LinxupJSONHelper {

  private LinxupJSONHelper() {
  }

  public static Position readPosition(JSONObject posObj) throws JSONException {
    double lat = posObj.getDouble("latitude"), lng = posObj.getDouble("longitude");
    Position.Builder positionBuilder = Position.builder()
        .date(new DateTime(posObj.getLong("date")))
        .direction(posObj.getInt("direction"))
        .speedMph(posObj.getInt("speed"))
        .speeding(posObj.optBoolean("speeding"))
        .estimatedSpeedLimit(posObj.optInt("estSpeedLimit"))
        .latLng(new LatLng(lat, lng))
        .batteryCharge(posObj.optString("battery"))
        .fuelLevel(posObj.optString("fuelLevel"))
        .driverId(posObj.optString("driverId"))
        .deviceNumber(posObj.optString("deviceNbr"))
        .simDeviceNumber(posObj.optString("simDeviceNumber"))
        .deviceTypeDescription(posObj.optString("deviceTypeDescription"))
        .vehicleLabel(posObj.getString("label"));
    return positionBuilder.build();
  }
}
