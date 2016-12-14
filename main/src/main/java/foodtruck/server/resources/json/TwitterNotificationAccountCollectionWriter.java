package foodtruck.server.resources.json;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

import foodtruck.model.TwitterNotificationAccount;

/**
 * @author aviolette
 * @since 12/5/12
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class TwitterNotificationAccountCollectionWriter extends CollectionWriter<TwitterNotificationAccount, TwitterNotificationAccountWriter> {
  /**
   * Constructs the collection writer
   * @param writer the writer used to output each entity
   */
  @Inject
  public TwitterNotificationAccountCollectionWriter(TwitterNotificationAccountWriter writer) {
    super(writer, TwitterNotificationAccount.class);
  }
}
