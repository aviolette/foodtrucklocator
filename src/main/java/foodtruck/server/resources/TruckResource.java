package foodtruck.server.resources;

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.socialmedia.ProfileSyncService;
import foodtruck.util.Clock;
import foodtruck.util.DateOnlyFormatter;

import static foodtruck.server.resources.Resources.requiresAdmin;

/**
 * @author aviolette@gmail.com
 * @since 6/13/12
 */
@Path("/trucks{view : (\\.[a-z]{3})?}")
public class TruckResource {
  private static final Logger log = Logger.getLogger(TruckResource.class.getName());
  public static final Predicate<Truck> NOT_HIDDEN = new Predicate<Truck>() {
    @Override public boolean apply(@Nullable Truck truck) {
      return !truck.isHidden();
    }
  };
  private final TruckDAO truckDAO;
  private final Clock clock;
  private final DateTimeZone zone;
  private final DateTimeFormatter formatter;
  private final ProfileSyncService profileSyncService;

  @Inject
  public TruckResource(TruckDAO truckDAO, Clock clock, DateTimeZone zone, @DateOnlyFormatter DateTimeFormatter formatter,
      ProfileSyncService profileSyncService) {
    this.truckDAO = truckDAO;
    this.clock = clock;
    this.zone = zone;
    this.formatter = formatter;
    this.profileSyncService = profileSyncService;
  }

  @GET
  @Produces({"application/json", "text/csv"})
  public JResponse<Collection<Truck>> getTrucks(@PathParam("view") String view, @QueryParam("active") final String active,
      @QueryParam("tag") final String filteredBy) {
    MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
    if (".csv".equals(view)) {
      mediaType = new MediaType("text", "csv");
    }
    Collection<Truck> response;
    if ("false".equals(active)) {
      response = truckDAO.findInactiveTrucks();
    } else if ("all".equals(active)) {
      response = truckDAO.findVisibleTrucks();
    } else {
      response = Strings.isNullOrEmpty(filteredBy) ? truckDAO.findActiveTrucks() : truckDAO.findByCategory(filteredBy);
    }
    return JResponse.ok(Collections2.filter(response, NOT_HIDDEN), mediaType).build();
  }

  @GET @Produces("application/json") @Path("{truckId}")
  public JResponse<Truck> getTruck(@PathParam("truckId") String truckId) {
    Truck t = truckDAO.findById(truckId);
    return JResponse.ok(t).build();
  }

  @POST @Path("{truckId}/mute")
  public void muteTruck(@PathParam("truckId") String truckId, @QueryParam("until") String until) {
    requiresAdmin();
    DateTime muteUntil = Strings.isNullOrEmpty(until) ?
        clock.currentDay().toDateTimeAtStartOfDay(zone).plusDays(1) :
        formatter.parseDateTime(until);
    Truck t = truckDAO.findById(truckId);
    t = Truck.builder(t).muteUntil(muteUntil)
        .build();
    truckDAO.save(t);
  }

  @POST @Path("{truckId}/unmute")
  public void unmuteTruck(@PathParam("truckId") String truckId) {
    requiresAdmin();
    Truck t = truckDAO.findById(truckId);
    t = Truck.builder(t).muteUntil(null)
        .build();
    truckDAO.save(t);
  }

  @DELETE @Path("{truckId}")
  public void delete(@PathParam("truckId") String truckId) {
    requiresAdmin();
    truckDAO.delete(truckId);
  }

  @POST @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
  public JResponse<Truck> createTruck(Truck truck) {
    Resources.requiresAdmin();
    if (truckDAO.findById(truck.getId()) != null) {
      throw new BadRequestException("POST can only be used , for creating objects");
    }
    return JResponse.ok(profileSyncService.createFromTwitter(truck)).build();
  }
}
