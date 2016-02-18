package foodtruck.server.resources.json;

import com.google.common.collect.ImmutableList;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import foodtruck.model.NotificationDeviceProfile;

import static org.junit.Assert.assertEquals;

/**
 * @author aviolette
 * @since 2/16/16
 */
public class NotificationDeviceProfileProviderTest {

  @Test
  public void testAsJSON() throws Exception {
    String testString = "{\"deviceToken\" : \"asdfasfd\",\n" +
        " \"truckIds\" : [\"thefatshallot\", \"theroosttruck\"],\n" +
        " \"locationNames\" : [\"600 West Chicago Avenue, Chicago, IL\"]\n" +
        "}";
    NotificationDeviceProfileProvider provider = new NotificationDeviceProfileProvider();
    NotificationDeviceProfile profile = provider.asJSON(new JSONObject(testString));
    assertEquals("asdfasfd", profile.getDeviceToken());
    assertEquals(ImmutableList.of("thefatshallot", "theroosttruck"), profile.getTruckIds());
    assertEquals(ImmutableList.of("600 West Chicago Avenue, Chicago, IL"), profile.getLocationNames());
  }
}