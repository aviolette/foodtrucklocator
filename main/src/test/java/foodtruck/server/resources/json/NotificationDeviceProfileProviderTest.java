package foodtruck.server.resources.json;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import foodtruck.model.NotificationDeviceProfile;

import static com.google.common.truth.Truth.assertThat;

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
    assertThat(profile.getDeviceToken()).isEqualTo("asdfasfd");
    assertThat(profile.getTruckIds()).containsExactly("thefatshallot", "theroosttruck");
    assertThat(profile.getLocationNames()).containsExactly("600 West Chicago Avenue, Chicago, IL");
  }
}