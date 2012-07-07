// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.server.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import foodtruck.dao.SystemStatDAO;
import foodtruck.model.StatVector;
import foodtruck.model.SystemStats;

/**
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
@Path("/stats")
@Produces(MediaType.APPLICATION_JSON)
public class StatsResource {
  private final SystemStatDAO systemStatDAO;

  @Inject
  public StatsResource(SystemStatDAO systemStatDAO) {
    this.systemStatDAO = systemStatDAO;
  }

  @GET @Path("{statList}")
  public JResponse<List<StatVector>> getStatsFor(@PathParam("statList") final String statName,
      @QueryParam("start") final long startTime, @QueryParam("end") final long endTime) {
    // TODO: assume for now statName only has one value
    List<StatVector> results = ImmutableList.of();

    List<SystemStats> stats = systemStatDAO.findWithinRange(startTime, endTime);



    return JResponse.ok(results).build();
  }
}
