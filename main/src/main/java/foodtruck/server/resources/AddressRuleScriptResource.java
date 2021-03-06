package foodtruck.server.resources;

import java.io.BufferedReader;
import java.io.StringReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import foodtruck.annotations.RequiresAdmin;
import foodtruck.dao.AddressRuleScriptDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.AddressRuleScript;
import foodtruck.model.Truck;
import foodtruck.schedule.JavascriptAddressExtractor;

/**
 * @author aviolette@gmail.com
 * @since 8/20/12
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/addressRules")
public class AddressRuleScriptResource {
  private final AddressRuleScriptDAO addressRuleDAO;
  private final JavascriptAddressExtractor extractor;
  private final TruckDAO truckDAO;

  @Inject
  public AddressRuleScriptResource(AddressRuleScriptDAO addressRuleDAO, JavascriptAddressExtractor extractor,
      TruckDAO truckDAO) {
    this.addressRuleDAO = addressRuleDAO;
    this.extractor = extractor;
    this.truckDAO = truckDAO;
  }

  @GET
  @RequiresAdmin
  public AddressRuleScript findSingleton() {
    return addressRuleDAO.find();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RequiresAdmin
  public void update(AddressRuleScript script) {
    Truck truck = this.truckDAO.findFirst();
    // attempt to execute the script on a small sentence and see if there are any issues before saving it
    try {
      extractor.executeScript("Hello World", truck, script.getScript());
      BufferedReader reader = new BufferedReader(new StringReader(script.getScript()));
      String firstLine = reader.readLine();
      Preconditions.checkState(firstLine.contains("Chicago"),
          "First line of script must contain correct city name");
    } catch (Exception e) {
      throw new WebApplicationException(Response.status(400)
          .entity(e.getMessage())
          .type(MediaType.TEXT_PLAIN_TYPE)
          .build());
    }
    addressRuleDAO.save(script);
  }
}
