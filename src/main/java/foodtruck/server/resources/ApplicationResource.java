package foodtruck.server.resources;

import java.util.Collection;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;

import foodtruck.dao.ApplicationDAO;
import foodtruck.model.Application;
import foodtruck.util.RandomString;
import static foodtruck.server.resources.Resources.requiresAdmin;

/**
 * @author aviolette
 * @since 1/25/13
 */
@Path("/applications") @Produces(MediaType.APPLICATION_JSON)
public class ApplicationResource {
  private final ApplicationDAO appDao;

  @Inject
  public ApplicationResource(ApplicationDAO appDao) {
    this.appDao = appDao;
  }

  @GET
  public Collection<Application> findAll() {
    requiresAdmin();
    return appDao.findAll();
  }

  @POST
  public void create(Application app) {
    requiresAdmin();
    app = Application.builder(app).appKey(RandomString.nextString(8)).build();
    appDao.save(app);
  }

  @DELETE @Path("{appKey}")
  public void delete(@PathParam("appKey") String appKey) {
    requiresAdmin();
    appDao.delete(appKey);
  }

  @PUT @Path("{appKey}")
  public void update(@PathParam("appKey") String appKey, Application app) {
    requiresAdmin();
    if (!appKey.equals(app.getAppKey())) {
      throw new BadRequestException("App keys not the same.");
    }
    appDao.save(app);
  }
}
