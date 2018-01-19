package foodtruck.alexa;

import com.sun.jersey.api.client.WebResource;

/**
 * @author aviolette
 * @since 1/19/18
 */
public interface AmazonConnectorFactory {
  AmazonConnector create(WebResource resource, DeviceAccess deviceAccess);
}
