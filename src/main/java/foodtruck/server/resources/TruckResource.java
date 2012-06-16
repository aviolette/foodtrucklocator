// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.server.resources;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since 6/13/12
 */
@Path("/trucks{view : (\\.[a-z]{3})?}")
public class TruckResource {
  private final TruckDAO truckDAO;

  @Inject
  public TruckResource(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }

  @GET
  @Produces({"application/json", "text/xml"})
  public JResponse<Collection<Truck>> getTrucks(@PathParam("view") String view) {
    MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
    if (".csv".equals(view)) {
      mediaType = new MediaType("text", "csv");
    }
    return JResponse.ok(truckDAO.findAll(), mediaType).build();
  }

  @POST @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
  public JResponse<Truck> createTruck(Truck truck) {
    Resources.requiresAdmin();
    if (truckDAO.findById(truck.getId()) != null) {
      throw new BadRequestException("POST can only be used for creating objects");
    }

    truckDAO.save(truck);
    return JResponse.ok(truck).build();
  }
}
