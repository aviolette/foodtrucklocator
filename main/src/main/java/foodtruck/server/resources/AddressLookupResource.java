package foodtruck.server.resources;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.AddressLookup;
import foodtruck.model.Location;
import foodtruck.schedule.AddressExtractor;

@Path("/address-lookup")
public class AddressLookupResource {

  private final AddressExtractor extractor;
  private final GeoLocator geoLocator;

  @Inject
  public AddressLookupResource(AddressExtractor extractor, GeoLocator geoLocator) {
    this.extractor = extractor;
    this.geoLocator = geoLocator;
  }

  @POST
  public JSONArray findAddresses(AddressLookup addressLookup) {
    List<String> values = extractor.parse(addressLookup.getText(), addressLookup.getTruck());
    return new JSONArray(values.stream()
        .map(item -> geoLocator.locate(item, GeolocationGranularity.BROAD))
        .filter(Objects::nonNull)
        .map(Location::getName)
        .collect(Collectors.toList()));
  }
}
