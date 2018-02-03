package foodtruck.alexa;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import foodtruck.util.ServiceException;

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
  public AddressResponse findAddress() throws ServiceException {
    try {
      log.info(deviceAccess.toString());
      String body = resource.uri(URI.create("/v1/devices/" + deviceAccess.getDeviceId() + "/settings/address"))
          .accept(MediaType.APPLICATION_JSON_TYPE)
          .header("Authorization", "Bearer " + deviceAccess.getAccessToken())
          .get(String.class);
      log.log(Level.INFO, body);
      return mapper.readValue(body, AddressResponse.class);
    } catch (IOException|UniformInterfaceException|ClientHandlerException e) {
      throw new ServiceException(e);
    }
  }
}
