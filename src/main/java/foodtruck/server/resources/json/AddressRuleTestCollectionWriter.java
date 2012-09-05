package foodtruck.server.resources.json;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

import foodtruck.model.AddressRuleTest;

/**
 * @author aviolette@gmail.com
 * @since 9/4/12
 */
@Provider @Produces(MediaType.APPLICATION_JSON)
public class AddressRuleTestCollectionWriter extends CollectionWriter<AddressRuleTest, AddressRuleTestWriter> {
  @Inject
  public AddressRuleTestCollectionWriter(AddressRuleTestWriter writer) {
    super(writer, AddressRuleTest.class);
  }
}
