package foodtruck.server.resources;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import foodtruck.dao.AddressRuleTestDAO;
import foodtruck.model.AddressRuleTest;

/**
 * @author aviolette@gmail.com
 * @since 9/4/12
 */
@Path("/addressTest") @Produces(MediaType.APPLICATION_JSON)
public class AddressRuleTestResource {
  private final AddressRuleTestDAO addressRuleDAO;

  @Inject
  public AddressRuleTestResource(AddressRuleTestDAO addressRuleDAO) {
    this.addressRuleDAO = addressRuleDAO;
  }

  @DELETE @Path("{id:\\d+}")
  public void delete(@PathParam("id") final long id) {
    addressRuleDAO.delete(id);
  }

  @GET
  public JResponse<Collection<AddressRuleTest>> findAll() {
    final Collection<AddressRuleTest> allByName = addressRuleDAO.findAllByName();
    return JResponse.ok(allByName).build();
  }

  @POST @Consumes(MediaType.APPLICATION_JSON)
  public void create(AddressRuleTest addressRule) {
    addressRuleDAO.save(addressRule);
  }
}
