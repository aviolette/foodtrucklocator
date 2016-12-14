package foodtruck.server.resources;

import java.util.Collection;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;

import foodtruck.annotations.RequiresAdmin;
import foodtruck.dao.TruckObserverDAO;
import foodtruck.model.TruckObserver;

/**
 * @author aviolette
 * @since 6/11/13
 */
@Path("/lookouts")
@Produces(MediaType.APPLICATION_JSON)
public class TruckObserverResource {
  private final TruckObserverDAO truckObserverDAO;

  @Inject
  public TruckObserverResource(TruckObserverDAO dao) {
    this.truckObserverDAO = dao;
  }

  @GET
  @RequiresAdmin
  public Collection<TruckObserver> findAll() {
    return truckObserverDAO.findAll();
  }

  @POST
  @RequiresAdmin
  public void create(TruckObserver truckObserver) {
    truckObserverDAO.save(truckObserver);
  }

  @DELETE
  @Path("{key}")
  @RequiresAdmin
  public void delete(@PathParam("key") String key) {
    truckObserverDAO.delete(key);
  }
}
