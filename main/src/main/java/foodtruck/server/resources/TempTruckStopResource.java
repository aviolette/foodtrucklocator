package foodtruck.server.resources;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.google.inject.Inject;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.server.security.SecurityChecker;

@Path("/temp-schedule")
public class TempTruckStopResource {

  private final TempTruckStopDAO dao;
  private final SecurityChecker checker;

  @Inject
  public TempTruckStopResource(TempTruckStopDAO dao, SecurityChecker checker) {
    this.dao = dao;
    this.checker = checker;
  }

  @POST
  public void create(TempTruckStop truckStop, @HeaderParam("x-ftf-secret") String secret) {
    checker.requiresSecret(secret);
    dao.save(truckStop);
  }
}
