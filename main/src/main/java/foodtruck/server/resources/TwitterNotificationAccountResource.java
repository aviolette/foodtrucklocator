package foodtruck.server.resources;

import java.util.Collection;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import foodtruck.annotations.RequiresAdmin;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.model.TwitterNotificationAccount;

import static foodtruck.server.resources.Resources.noCache;

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

  @DELETE
  @Path("{id}")
  @RequiresAdmin
  public void delete(@PathParam("id") long id) {
    dao.delete(id);
  }

  @PUT
  @Path("{id}")
  @RequiresAdmin
  public void update(@PathParam("id") long id, TwitterNotificationAccount account) {
    dao.save(account);
  }

  @GET
  @RequiresAdmin
  public JResponse<Collection<TwitterNotificationAccount>> findAll() {
    return noCache(JResponse.ok((Collection<TwitterNotificationAccount>) dao.findAll())).build();
  }

  @POST
  @RequiresAdmin
  public void create(TwitterNotificationAccount account) {
    dao.save(account);
  }
}
