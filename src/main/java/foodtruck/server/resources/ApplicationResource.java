package foodtruck.server.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import foodtruck.dao.ApplicationDAO;
import foodtruck.model.Application;
import foodtruck.model.ApplicationWithUsageCounts;
import foodtruck.util.RandomString;

import static foodtruck.server.resources.Resources.requiresAdmin;

/**
 * @author aviolette
 * @since 1/25/13
 */
@Path("/applications") @Produces(MediaType.APPLICATION_JSON)
public class ApplicationResource {
  private static final Logger log = Logger.getLogger(ApplicationResource.class.getName());
  private final ApplicationDAO appDao;
  private final MemcacheService memcacheService;

  @Inject
  public ApplicationResource(ApplicationDAO appDao, MemcacheService memcacheService) {
    this.appDao = appDao;
    this.memcacheService = memcacheService;
  }

  @GET
  public Iterable<ApplicationWithUsageCounts> findAll() {
    requiresAdmin();
    return Iterables.transform(appDao.findAll(), new Function<Application, ApplicationWithUsageCounts>() {
      public ApplicationWithUsageCounts apply(Application application) {
        long count =  0;
        try {
          String key = "service.access.daily." + application.getKey();
          if (memcacheService.contains(key)) {
            count = (Long) memcacheService.get(key);
          }
        } catch (Exception e) {
          log.log(Level.WARNING, e.getMessage());
        }
        return new ApplicationWithUsageCounts(application, count);
      }
    });
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
