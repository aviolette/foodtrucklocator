package foodtruck.server.resources;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.SpecialsDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.LocationWithDailyData;
import foodtruck.monitoring.Monitored;
import foodtruck.util.Clock;
import foodtruck.util.ServiceException;

/**
 * @author aviolette
 * @since 3/6/13
 */
@Path("/locations") @Produces(MediaType.APPLICATION_JSON)
public class LocationResource {
  private final GeoLocator locator;
  private final AuthorizationChecker authorizationChecker;
  private final LocationDAO locationDAO;
  private final SpecialsDAO specialsDAO;
  private final Clock clock;

  @Inject
  public LocationResource(GeoLocator locator, AuthorizationChecker checker, LocationDAO locationDAO,
      SpecialsDAO specialsDAO, Clock clock) {
    this.locator = locator;
    this.authorizationChecker = checker;
    this.locationDAO = locationDAO;
    this.specialsDAO = specialsDAO;
    this.clock = clock;
  }

  @GET @Path("designated")
  public Collection<Location> findStops(@QueryParam("appKey") String appKey) {
    authorizationChecker.requireAppKey(appKey);
    return locationDAO.findDesignatedStops();
  }

  @GET @Path("{location}") @Monitored
  public JResponse<LocationWithDailyData> findLocation(@PathParam("location") String locationName,
      @QueryParam("appKey") String appKey) {
    authorizationChecker.requireAppKey(appKey);
    try {
      Location loc;
      if (locationName.matches("^\\d+$")) {
        loc = locationDAO.findById(Long.parseLong(locationName));
      } else {
        loc = locator.locate(locationName, GeolocationGranularity.NARROW);
      }
      if (loc != null) {
        LocationWithDailyData locationWithDailyData =
            new LocationWithDailyData(loc, specialsDAO.findByLocationAndDay(loc.getName(), clock.currentDay() ));
        return JResponse.ok(locationWithDailyData).build();
      }
      return JResponse.<LocationWithDailyData>status(Response.Status.NOT_FOUND).build();
    } catch (ServiceException se) {
      return JResponse.<LocationWithDailyData>status(Response.Status.BAD_REQUEST).build();
    }
  }
}
