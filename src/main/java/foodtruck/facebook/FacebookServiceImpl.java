package foodtruck.facebook;

import java.net.URI;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 2/18/13
 */
public class FacebookServiceImpl implements FacebookService {
  private static final Logger log = Logger.getLogger(FacebookServiceImpl.class.getName());
  private final TruckDAO truckDAO;
  private final WebResource facebookResource;
  private final Pattern pageUrlPattern = Pattern.compile("/pages/(.*)/(\\d+)");

  @Inject
  public FacebookServiceImpl(TruckDAO dao, @FacebookEndpoint WebResource facebookResource) {
    this.truckDAO = dao;
    this.facebookResource = facebookResource;
  }

  @Override public void syncTruckData() {
    for (Truck truck : truckDAO.findActiveTrucks()) {
      if (Strings.isNullOrEmpty(truck.getFacebook())) {
        continue;
      }
      try {
        truck = syncTruck(truck);
      } catch (JSONException e) {
        throw Throwables.propagate(e);
      } catch (Exception e) {
        log.warning(e.getMessage());
        continue;
      }
      truckDAO.save(truck);
    }
  }

  private Truck syncTruck(Truck truck) throws JSONException {
    String uri = truck.getFacebook();
    Matcher m = pageUrlPattern.matcher(uri);
    if (m.find()) {
      uri = "/" + m.group(2);
    }
    String response = facebookResource.uri(URI.create(uri))
        .get(String.class);
    JSONObject responseObj = new JSONObject(response);
    String about = responseObj.optString("about");
    if (Strings.isNullOrEmpty(about)) {
      about = responseObj.optString("description");
    }
    about = "<blockquote>" + about + "</blockquote>";
    String facebookId = responseObj.getString("id");
    truck = Truck.builder(truck).facebookPageId(facebookId).description(about).build();
    return truck;
  }
}
