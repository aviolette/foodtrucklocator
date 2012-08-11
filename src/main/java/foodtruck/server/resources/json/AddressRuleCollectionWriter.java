package foodtruck.server.resources.json;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

import foodtruck.model.AddressRule;

/**
 * @author aviolette@gmail.com
 * @since 8/20/12
 */
@Provider @Produces(MediaType.APPLICATION_JSON)
public class AddressRuleCollectionWriter extends CollectionWriter<AddressRule, AddressRuleWriter> {
  @Inject
  public AddressRuleCollectionWriter(AddressRuleWriter writer) {
    super(writer, AddressRule.class);
  }
}
