package foodtruck.server.resources;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.annotations.RequiresAdmin;
import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.TrackingDevice;
import foodtruck.model.Truck;
import foodtruck.profile.ProfileSyncService;
import foodtruck.server.security.SecurityChecker;
import foodtruck.time.Clock;
import foodtruck.time.DateOnlyFormatter;

/**
 * @author aviolette@gmail.com
 * @since 6/13/12
 */
@Path("/trucks{view : (\\.[a-z]{3})?}")
public class TruckResource {
  private final TruckDAO truckDAO;
  private final Clock clock;
  private final DateTimeZone zone;
  private final DateTimeFormatter formatter;
  private final ProfileSyncService profileSyncService;
  private final DailySpecialResourceFactory dailySpecialResourceFactory;
  private final TrackingDeviceDAO trackingDeviceDAO;
  private final SecurityChecker securityChecker;

  @Inject
  public TruckResource(TruckDAO truckDAO, Clock clock, DateTimeZone zone,
      @DateOnlyFormatter DateTimeFormatter formatter, ProfileSyncService profileSyncService,
      DailySpecialResourceFactory dailySpecialResourceFactory, TrackingDeviceDAO trackingDeviceDAO,
      SecurityChecker securityChecker) {
    this.truckDAO = truckDAO;
    this.clock = clock;
    this.zone = zone;
    this.formatter = formatter;
    this.profileSyncService = profileSyncService;
    this.dailySpecialResourceFactory = dailySpecialResourceFactory;
    this.trackingDeviceDAO = trackingDeviceDAO;
    this.securityChecker = securityChecker;
  }

  @GET
  @Produces({"application/json", "text/csv", "text/plain"})
  public JResponse<List<Truck>> getTrucks(@PathParam("view") String view,
      @QueryParam("active") final String active, @QueryParam("tag") final String filteredBy) {
    MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
    if (".csv".equals(view)) {
      mediaType = new MediaType("text", "csv");
    } else if (".txt".equals(view)) {
      mediaType = MediaType.TEXT_PLAIN_TYPE;
    }
    Collection<Truck> response;
    if ("false".equals(active)) {
      response = truckDAO.findInactiveTrucks();
    } else if ("all".equals(active)) {
      response = truckDAO.findVisibleTrucks();
    } else {
      response = Strings.isNullOrEmpty(filteredBy) ? truckDAO.findActiveTrucks() : truckDAO.findByCategory(filteredBy);
    }
    return JResponse.ok(response.stream()
        .filter(Truck::isVisible)
        .collect(Collectors.toList()), mediaType)
        .build();
  }

  @GET
  @Produces("application/json")
  @Path("{truckId}")
  public JResponse<Truck> getTruck(@PathParam("truckId") String truckId) {
    Truck t = truckDAO.findByIdOpt(truckId).orElseThrow(() -> new WebApplicationException(404));
    return JResponse.ok(t)
        .build();
  }

  @POST
  @Path("{truckId}/mute")
  @RequiresAdmin
  public void muteTruck(@PathParam("truckId") String truckId, @QueryParam("until") String until) {
    DateTime muteUntil = Strings.isNullOrEmpty(until) ? clock.currentDay()
        .toDateTimeAtStartOfDay(zone)
        .plusDays(1) : formatter.parseDateTime(until);
    truckDAO.findByIdOpt(truckId).ifPresent(truck -> truckDAO.save(Truck.builder(truck)
        .muteUntil(muteUntil)
        .build()));
  }

  @POST
  @Path("{truckId}/unmute")
  @RequiresAdmin
  public void unmuteTruck(@PathParam("truckId") String truckId) {
    truckDAO.findByIdOpt(truckId).ifPresent(truck -> truckDAO.save(Truck.builder(truck)
        .muteUntil(null)
        .build()));
  }

  @DELETE
  @Path("{truckId}")
  @RequiresAdmin
  public void delete(@PathParam("truckId") String truckId) {
    truckDAO.delete(truckId);
  }

  @Path("{truckId}/specials")
  public DailySpecialResource getDailySpecialResource(@PathParam("truckId") String truckId) {
    return dailySpecialResourceFactory.create(truckDAO.findByIdOpt(truckId)
        .orElseThrow(() -> new WebApplicationException(404)));
  }

  @GET
  @Path("{truckId}/beacons")
  public List<TrackingDevice> findBeacons(@PathParam("truckId") String truckId) {
    securityChecker.requiresLoggedInAs(truckId);
    return trackingDeviceDAO.findByTruckId(truckId);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RequiresAdmin
  public JResponse<Truck> createTruck(Truck truck) {
    truckDAO.findByIdOpt(truck.getId())
        .ifPresent(t -> {
          throw new BadRequestException("POST can only be used , for creating objects");
        });
    return JResponse.ok(profileSyncService.createFromTwitter(truck))
        .build();
  }
}
