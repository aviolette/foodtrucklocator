package foodtruck.linxup;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;

import foodtruck.util.ServiceException;

/**
 * @author aviolette
 * @since 7/25/16
 */
class LinxupConnectorImpl implements LinxupConnector {
  private final WebResource resource;
  private final LinxupMapRequest mapRequest;

  @Inject
  public LinxupConnectorImpl(@LinxupEndpoint WebResource resource, LinxupMapRequest mapRequest) {
    this.resource = resource;
    this.mapRequest = mapRequest;
  }

  @Override
  public List<Position> findPositions() {
    LinxupMapResponse response = resource
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
        .header(HttpHeaders.ACCEPT_ENCODING, "gzip,deflate")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .entity(mapRequest)
        .post(LinxupMapResponse.class);
    if (response.isSuccessful()) {
      return response.getPositions();
    }
    throw new ServiceException(response.getError());
  }
}
