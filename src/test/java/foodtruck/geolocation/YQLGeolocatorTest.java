package foodtruck.geolocation;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import foodtruck.model.Location;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author aviolette
 * @since 4/29/13
 */
public class YQLGeolocatorTest extends EasyMockSupport {

  private YQLResource resource;
  private YQLGeolocator yqlGeolocator;

  @Before
  public void before() {
    resource = createMock(YQLResource.class);
    yqlGeolocator = new YQLGeolocator(resource);

  }
  @Test
  public void findLocation() throws JSONException {
    String location = "1504 North Western Avenue, Chicago";
    JSONObject jsonObject = new JSONObject("{\"query\": {\n" +
        "  \"count\": 1,\n" +
        "  \"created\": \"2013-04-29T11:43:06Z\",\n" +
        "  \"lang\": \"en-US\",\n" +
        "  \"results\": {\"Result\": {\n" +
        "    \"quality\": \"87\",\n" +
        "    \"latitude\": \"41.908741\",\n" +
        "    \"longitude\": \"-87.687149\",\n" +
        "    \"offsetlat\": \"41.90873\",\n" +
        "    \"offsetlon\": \"-87.687561\",\n" +
        "    \"radius\": \"400\",\n" +
        "    \"name\": null,\n" +
        "    \"line1\": \"1504 N Western Ave\",\n" +
        "    \"line2\": \"Chicago, IL 60622-1746\",\n" +
        "    \"line3\": null,\n" +
        "    \"line4\": \"United States\",\n" +
        "    \"house\": \"1504\",\n" +
        "    \"street\": \"N Western Ave\",\n" +
        "    \"xstreet\": null,\n" +
        "    \"unittype\": null,\n" +
        "    \"unit\": null,\n" +
        "    \"postal\": \"60622-1746\",\n" +
        "    \"neighborhood\": null,\n" +
        "    \"city\": \"Chicago\",\n" +
        "    \"county\": \"Cook County\",\n" +
        "    \"state\": \"Illinois\",\n" +
        "    \"country\": \"United States\",\n" +
        "    \"countrycode\": \"US\",\n" +
        "    \"statecode\": \"IL\",\n" +
        "    \"countycode\": null,\n" +
        "    \"uzip\": \"60622\",\n" +
        "    \"hash\": \"3CA8E5C34858988D\",\n" +
        "    \"woeid\": \"12784276\",\n" +
        "    \"woetype\": \"11\"\n" +
        "  }}\n" +
        "}}\n");
    expect(resource.findLocation(location, false)).andReturn(jsonObject);
    replayAll();
    Location loc = yqlGeolocator.locate(location, GeolocationGranularity.NARROW);
    assertNotNull(loc);
    assertEquals(location, loc.getName());
    assertEquals(41.908741, loc.getLatitude(), 0.00001);
    assertEquals(-87.687149, loc.getLongitude(), 0.00001);
    verifyAll();
  }
}
