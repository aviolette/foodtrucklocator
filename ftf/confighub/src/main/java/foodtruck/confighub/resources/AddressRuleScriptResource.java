package foodtruck.confighub.resources;

import java.util.logging.Logger;

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
 * @author aviolette
 * @since 10/13/16
 */
@Produces(MediaType.APPLICATION_JSON) @Path("/address_rule_scripts")
public class AddressRuleScriptResource {
  private final AddressRuleScriptDAO addressRuleScriptDAO;

  @Inject
  public AddressRuleScriptResource(AddressRuleScriptDAO addressRuleScriptDAO) {
    this.addressRuleScriptDAO = addressRuleScriptDAO;
  }

  @GET
  public AddressRuleScript findSingleton() {
    return addressRuleScriptDAO.find();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void update(AddressRuleScript script) {
    script.validate();
    addressRuleScriptDAO.save(script);
  }
}
