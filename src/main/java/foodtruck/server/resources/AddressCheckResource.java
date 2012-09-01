package foodtruck.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Throwables;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.schedule.AddressExtractor;

/**
 * @author aviolette@gmail.com
 * @since 8/30/12
 */
@Path("/addressCheck") @Produces(MediaType.APPLICATION_JSON)
public class AddressCheckResource {
  private final AddressExtractor addressExtractor;
  private final TruckDAO truckDAO;

  @Inject
  public AddressCheckResource(AddressExtractor addressExtractor, TruckDAO dao) {
    this.addressExtractor = addressExtractor;
    this.truckDAO = dao;
  }

  @GET
  public JSONObject match(@QueryParam("q") final String query,
      @QueryParam("truck") final String truckId) {
    try {
      Truck truck = truckDAO.findById(truckId);
      JSONArray results = new JSONArray();
      for (String result : addressExtractor.parse(query, truck)) {
        results.put(result);
      }
      return new JSONObject().put("results", results);
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }
}
