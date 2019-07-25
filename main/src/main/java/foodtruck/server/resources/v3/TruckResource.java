package foodtruck.server.resources.v3;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import foodtruck.annotations.UseJackson;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.monitoring.Monitored;

@Path("/v3/trucks")
public class TruckResource {

  private final TruckDAO truckDAO;

  @Inject
  public TruckResource(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }


  @GET
  @Monitored
  @UseJackson
  public JResponse<List<Truck>> findTrucks() {
    return JResponse.ok(truckDAO.findActiveTrucks().stream()
        .filter(Truck::isVisible)
        .collect(Collectors.toList()), MediaType.APPLICATION_JSON_TYPE)
        .build();

  }

  @GET
  @Monitored
  @Path("{truckId}")
  @UseJackson
  public JResponse<Truck> findTruck(@PathParam("truckId") String truckId) {
    Truck t = truckDAO.findByIdOpt(truckId).orElseThrow(() -> new WebApplicationException(404));
    return JResponse.ok(t).build();
  }
}
