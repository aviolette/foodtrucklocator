package foodtruck.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.util.ServiceException;

/**
 * @author aviolette
 * @since 3/6/13
 */
@Path("/locations") @Produces(MediaType.APPLICATION_JSON)
public class LocationResource {
  private final GeoLocator locator;
  private final AuthorizationChecker authorizationChecker;

  @Inject
  public LocationResource(GeoLocator locator, AuthorizationChecker checker) {
    this.locator = locator;
    this.authorizationChecker = checker;
  }

  @GET @Path("{location}")
  public JResponse<Location> findLocation(@PathParam("location") String locationName,
      @QueryParam("appKey") String appKey) {
    authorizationChecker.requireAppKey(appKey);
    try {
      Location loc = locator.locate(locationName, GeolocationGranularity.BROAD);
      if (loc != null) {
        return JResponse.ok(loc).build();
      }
      return JResponse.<Location>status(Response.Status.NOT_FOUND).build();
    } catch (ServiceException se) {
      return JResponse.<Location>status(Response.Status.BAD_REQUEST).build();
    }
  }
}
