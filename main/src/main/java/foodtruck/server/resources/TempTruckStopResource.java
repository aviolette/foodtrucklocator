package foodtruck.server.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.google.inject.Inject;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.model.TempTruckStop;

@Path("/temp-schedule")
public class TempTruckStopResource {

  private final TempTruckStopDAO dao;

  @Inject
  public TempTruckStopResource(TempTruckStopDAO dao) {
    this.dao = dao;
  }

  @POST
  public void create(TempTruckStop truckStop) {
    dao.save(truckStop);
  }
}
