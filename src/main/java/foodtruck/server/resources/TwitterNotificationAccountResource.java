package foodtruck.server.resources;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.model.TwitterNotificationAccount;
import static foodtruck.server.resources.Resources.noCache;
import static foodtruck.server.resources.Resources.requiresAdmin;

/**
 * @author aviolette
 * @since 12/4/12
 */
@Path("/notifications")
public class TwitterNotificationAccountResource {
  private final TwitterNotificationAccountDAO dao;

  @Inject
  public TwitterNotificationAccountResource(TwitterNotificationAccountDAO dao) {
    this.dao = dao;
  }

  @GET
  public JResponse<Collection<TwitterNotificationAccount>> findAll() {
    requiresAdmin();
    return noCache(JResponse.ok(dao.findAll())).build();
  }

  @POST
  public void create(TwitterNotificationAccount account) {
    requiresAdmin();
    dao.save(account);
  }
}
