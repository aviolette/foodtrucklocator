package foodtruck.server.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;

import foodtruck.dao.AddressRuleScriptDAO;
import foodtruck.model.AddressRuleScript;

/**
 * @author aviolette@gmail.com
 * @since 8/20/12
 */
@Produces(MediaType.APPLICATION_JSON) @Path("/addressRules")
public class AddressRuleScriptResource {
  private final AddressRuleScriptDAO addressRuleDAO;

  @Inject
  public AddressRuleScriptResource(AddressRuleScriptDAO addressRuleDAO) {
    this.addressRuleDAO = addressRuleDAO;
  }

  @GET
  public AddressRuleScript findSingleton() {
    return addressRuleDAO.find();
  }

  @POST @Consumes(MediaType.APPLICATION_JSON)
  public void update(AddressRuleScript script) {
    addressRuleDAO.save(script);
  }
}
