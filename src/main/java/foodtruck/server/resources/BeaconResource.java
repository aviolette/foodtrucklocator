package foodtruck.server.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.Inject;

import foodtruck.dao.TruckDAO;
import foodtruck.beaconnaise.BeaconResponse;
import foodtruck.beaconnaise.BeaconSignal;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.truckstops.FoodTruckStopService;

/**
 * @author aviolette
 * @since 7/20/13
 */
@Path("/beacon") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
public class BeaconResource {
  private final TruckDAO truckDAO;
  private final FoodTruckStopService stopService;
  private static final Logger log = Logger.getLogger(BeaconResource.class.getName());
  @Inject
  public BeaconResource(TruckDAO truckDAO, FoodTruckStopService stopService) {
    this.truckDAO = truckDAO;
    this.stopService = stopService;
  }

  @POST
  public BeaconResponse receiveSignal(BeaconSignal signal) {
    log.log(Level.INFO,  "Received beacon: " + signal);
    UserService userService = UserServiceFactory.getUserService();
    Truck truck = truckDAO.findById(signal.getTruckId());
    if (!truck.getBeaconnaiseEmails().contains(userService.getCurrentUser().getEmail())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    final TruckStop stop = stopService.handleBeacon(signal);
    return new BeaconResponse(stop);
  }
}
