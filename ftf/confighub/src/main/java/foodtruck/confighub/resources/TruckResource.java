package foodtruck.confighub.resources;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 10/13/16
 */
@Produces(MediaType.APPLICATION_JSON) @Path("/trucks")
public class TruckResource {
  private final TruckDAO truckDAO;

  @Inject
  public TruckResource(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }

  @GET
  public List<Truck> findAll(@Context UriInfo ui) {
    MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    if (queryParams.size() == 0) {
      return truckDAO.findAll();
    } else if (queryParams.containsKey("twitterHandle")) {
      return ImmutableList.copyOf(truckDAO.findByTwitterId(queryParams.getFirst("twitterHandle")));
    } else if (queryParams.containsKey("name")) {
      Truck truck = truckDAO.findByName(queryParams.getFirst("name"));
      if (truck == null) {
        return ImmutableList.of();
      }
      return ImmutableList.of(truck);
    } else {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  @GET @Path("{truckId}")
  public Truck find(@PathParam("truckId") String truckId) {
    Truck truck = truckDAO.findById(truckId);
    if (truck == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return truck;
  }

  @POST
  public void save(Truck truck) {
    truckDAO.save(truck);
  }

  @DELETE @Path("{truckId}")
  public void delete(@PathParam("truckId") String truckId) {
    if (truckDAO.findById(truckId) == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    truckDAO.delete(truckId);
  }
}
