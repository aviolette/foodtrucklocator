package foodtruck.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.google.inject.Inject;

import foodtruck.annotations.RequiresAdmin;
import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 10/7/18
 */
@Path("/reverse_lookup")
public class ReverseLookupResource {

  private final GeoLocator locator;

  @Inject
  public ReverseLookupResource(GeoLocator locator) {
    this.locator = locator;
  }

  @GET
  @RequiresAdmin
  public Location find(@QueryParam("latlng") String latLng) {
    String result[] = latLng.split(",");
    return locator.reverseLookup(Location.builder().lat(Double.valueOf(result[0])).lng(Double.valueOf(result[1])).build());
  }
}
