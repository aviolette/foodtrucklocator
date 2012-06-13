// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.server.resources;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since 6/13/12
 */
@Path("/trucks")
@Produces("application/json")
public class TruckResource {
  private final TruckDAO truckDAO;

  @Inject
  public TruckResource(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }

  @GET
  public JResponse<Collection<Truck>> getTrucks() {
    return JResponse.ok(truckDAO.findAll()).build();
  }
}
