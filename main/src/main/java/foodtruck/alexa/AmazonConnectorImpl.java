package foodtruck.alexa;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sun.jersey.api.client.WebResource;

/**
 * @author aviolette
 * @since 1/6/18
 */
public class AmazonConnectorImpl implements AmazonConnector {

  private static final Logger log = Logger.getLogger(AmazonConnectorImpl.class.getName());

  private final WebResource resource;
  private final DeviceAccess deviceAccess;
  private final ObjectMapper mapper;

  @Inject
  public AmazonConnectorImpl(ObjectMapper mapper, @Assisted WebResource resource,
      @Assisted DeviceAccess deviceAccess) {
    this.resource = resource;
    this.deviceAccess = deviceAccess;
    this.mapper = mapper;
  }

  @Override
  @Nullable
  public AddressResponse findAddress() {
    String body = resource.uri(URI.create("/v1/devices/" + deviceAccess.getDeviceId() + "/settings/address/countryAndPostalCode"))
        .accept(MediaType.APPLICATION_JSON_TYPE)
        .header("Authorization", "Bearer " + deviceAccess.getAccessToken())
        .get(String.class);

    log.log(Level.INFO, body);
    try {
      return mapper.readValue(body, AddressResponse.class);
    } catch (IOException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
    return null;
  }
}
