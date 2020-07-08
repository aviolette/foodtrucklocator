package foodtruck.server.vendor;

import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.annotations.GoogleJavascriptApiKey;
import foodtruck.dao.LinxupAccountDAO;
import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 11/26/16
 */
public class BeaconServletHelper {

  private final LocationDAO locationDAO;
  private final LinxupAccountDAO linxupAccountDAO;
  private final String googleApiKey;

  @Inject
  public BeaconServletHelper(LocationDAO locationDAO, LinxupAccountDAO linxupAccountDAO, @GoogleJavascriptApiKey String googleApiKey) {
    this.locationDAO = locationDAO;
    this.linxupAccountDAO = linxupAccountDAO;
    this.googleApiKey = googleApiKey;
  }

  private JSONArray beaconsToJson(Truck truck) {
    return new JSONArray(truck .getBlacklistLocationNames().stream()
        .map(input -> {
          Location location = locationDAO.findByName(input).orElse(null);
          if (location == null) {
            return null;
          }
          try {
            return new JSONObject().put("name", input)
                .put("latitude", location.getLatitude())
                .put("longitude", location.getLongitude())
                .put("radius", location.getRadius());
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList()));
  }

  public void seedRequest(HttpServletRequest request, Truck truck) {
    request.setAttribute("blacklist", beaconsToJson(truck));
    request.setAttribute("linxupAccount", linxupAccountDAO.findByTruck(truck.getId()));
    request.setAttribute("categories", new JSONArray(truck.getCategories()));
    request.setAttribute("googleApiKey", googleApiKey);
  }
}
