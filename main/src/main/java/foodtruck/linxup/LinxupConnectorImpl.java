package foodtruck.linxup;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;

import org.joda.time.DateTime;

import foodtruck.model.LinxupAccount;
import foodtruck.monitoring.Monitored;
import foodtruck.util.ServiceException;

/**
 * @author aviolette
 * @since 7/25/16
 */
class LinxupConnectorImpl implements LinxupConnector {
  private final WebResource resource;
  private final WebResource tripResource;

  @Inject
  public LinxupConnectorImpl(@LinxupEndpoint WebResource resource, @LinxupMapHistoryEndpoint WebResource tripResource) {
    this.resource = resource;
    this.tripResource = tripResource;
  }

  @Override
  @Monitored
  public List<Position> findPositions(LinxupAccount account) throws IOException, ServiceException {
    LinxupMapResponse response;
    try {
      response = resource.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
          .header(HttpHeaders.ACCEPT_ENCODING, "gzip,deflate")
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
          .entity(new LinxupMapRequest(account.getUsername(), account.getPassword()))
          .post(LinxupMapResponse.class);
    } catch (RuntimeException rte) {
      throw new IOException(rte.getMessage());
    }
    if (response.isSuccessful()) {
      return response.getPositions();
    }
    throw new ServiceException(response.getError());
  }

  @Override
  @Monitored
  public LinxupMapHistoryResponse tripList(LinxupAccount account, DateTime start, DateTime end, String deviceId) {
    return tripResource.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
        .header(HttpHeaders.ACCEPT_ENCODING, "gzip,deflate")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .entity(new LinxupMapHistoryRequest(account.getUsername(), account.getPassword(), start, end, deviceId))
        .post(LinxupMapHistoryResponse.class);
  }
}
