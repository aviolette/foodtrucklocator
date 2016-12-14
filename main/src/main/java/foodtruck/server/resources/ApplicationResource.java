package foodtruck.server.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import foodtruck.annotations.RequiresAdmin;
import foodtruck.dao.ApplicationDAO;
import foodtruck.model.Application;
import foodtruck.model.ApplicationWithUsageCounts;
import foodtruck.monitoring.Counter;
import foodtruck.monitoring.DailyScheduleCounter;
import foodtruck.util.RandomString;

/**
 * @author aviolette
 * @since 1/25/13
 */
@Path("/applications")
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationResource {
  private final ApplicationDAO appDao;
  private final Counter counter;

  @Inject
  public ApplicationResource(ApplicationDAO appDao, @DailyScheduleCounter Counter counter) {
    this.appDao = appDao;
    this.counter = counter;
  }

  @GET
  @RequiresAdmin
  public Iterable<ApplicationWithUsageCounts> findAll() {
    return Iterables.transform(appDao.findAll(), new Function<Application, ApplicationWithUsageCounts>() {
      public ApplicationWithUsageCounts apply(Application application) {
        return new ApplicationWithUsageCounts(application, counter.getCount(application.getAppKey()));
      }
    });
  }

  @POST
  @RequiresAdmin
  public void create(Application app) {
    app = Application.builder(app)
        .appKey(RandomString.nextString(8))
        .build();
    try {
      app.validate();
    } catch (IllegalArgumentException iae) {
      throw new WebApplicationException(iae, 400);
    }
    appDao.save(app);
  }

  @DELETE
  @Path("{appKey}")
  @RequiresAdmin
  public void delete(@PathParam("appKey") String appKey) {
    appDao.delete(appKey);
  }

  @PUT
  @Path("{appKey}")
  @RequiresAdmin
  public void update(@PathParam("appKey") String appKey, Application app) {
    try {
      app.validate();
    } catch (IllegalArgumentException iae) {
      throw new WebApplicationException(iae, 400);
    }
    if (!appKey.equals(app.getAppKey())) {
      throw new BadRequestException("App keys not the same.");
    }
    appDao.save(app);
  }
}
