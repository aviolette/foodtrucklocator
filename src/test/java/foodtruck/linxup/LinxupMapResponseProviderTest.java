package foodtruck.linxup;

import java.io.ByteArrayInputStream;

import com.javadocmd.simplelatlng.LatLng;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author aviolette
 * @since 7/26/16
 */
public class LinxupMapResponseProviderTest {
  private LinxupMapResponseProvider provider;

  @Before
  public void before() {
    provider = new LinxupMapResponseProvider();
  }

  @Test
  public void readFrom() throws Exception {
    String response = "{\"data\":{\"positions\":[{\"date\":1381254908000,\"label\":\"Vehicle One\",\"latitude\":38.6558,\"longitude\":-90.5618,\"altitude\":174,\"heading\":\"S\",\"direction\":163,\"speed\":80,\"speeding\":true,\"behaviorCd\":\"HAC\",\"estSpeedLimit\":70},{\"date\":1383775826000,\"label\":\"Vehicle Two\",\"latitude\":38.598,\"longitude\":-90.4347,\"altitude\":174,\"heading\":\"N\",\"direction\":0,\"speed\":0,\"speeding\":null,\"behaviorCd\":null,\"estSpeedLimit\":null},{\"date\":1383743827000,\"label\":\"Vehicle Three\",\"latitude\":38.6553,\"longitude\":-90.5619,\"altitude\":150,\"heading\":\"SE\",\"direction\":146,\"speed\":45,\"speeding\":null,\"behaviorCd\":null,\"estSpeedLimit\":null}]},\"responseType\":\"Success\"}";
    LinxupMapResponse providerResponse = provider.readFrom(null, null, null, null, null, new ByteArrayInputStream(response.getBytes()));
    assertNotNull(providerResponse);
    assertTrue(providerResponse.isSuccessful());
    assertNull(providerResponse.getError());
    assertEquals(3, providerResponse.getPositions().size());
    Position pos = providerResponse.getPositions().get(0);
    assertEquals("Vehicle One", pos.getVehicleLabel());
    assertEquals(new LatLng(38.6558, -90.5618), pos.getLatLng());
    assertEquals(174, pos.getAltitude());
    assertEquals(163, pos.getDirection());
    assertEquals(80, pos.getSpeedMph());
    assertEquals(true, pos.isSpeeding());
    assertEquals(70, pos.getEstimatedSpeedLimit());
    assertEquals(new DateTime(2013, 10, 8, 12, 55, 8, 0), pos.getDate());
  }
}