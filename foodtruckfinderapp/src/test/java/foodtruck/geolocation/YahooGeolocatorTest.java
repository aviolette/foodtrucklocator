package foodtruck.geolocation;

import org.codehaus.jettison.json.JSONObject;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import foodtruck.model.Location;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author aviolette@gmail.com
 * @since 10/18/11
 */
public class YahooGeolocatorTest extends EasyMockSupport {
  private YahooResource resource;
  private YahooGeolocator yahooGeolocator;

  @Before
  public void before() {
    resource = createMock(YahooResource.class);
    yahooGeolocator = new YahooGeolocator(resource);
  }

  @Test
  public void testCityResultShouldReturnNull() throws Exception {
    JSONObject returnCode = new JSONObject(
        "{\"ResultSet\":{\"version\":\"1.0\",\"Error\":0,\"ErrorMessage\":\"No error\",\"Locale\":\"us_US\",\"Quality\":87,\"Found\":1,\"Results\":[{\"quality\":39,\"latitude\":\"41.849606\",\"longitude\":\"-87.682827\",\"offsetlat\":\"41.884151\",\"offsetlon\":\"-87.632408\",\"radius\":33600,\"name\":\"\",\"line1\":\"\",\"line2\":\"Chicago, IL\",\"line3\":\"\",\"line4\":\"United States\",\"house\":\"\",\"street\":\"\",\"xstreet\":\"\",\"unittype\":\"\",\"unit\":\"\",\"postal\":\"\",\"neighborhood\":\"\",\"city\":\"Chicago\",\"county\":\"Cook County\",\"state\":\"Illinois\",\"country\":\"United States\",\"countrycode\":\"US\",\"statecode\":\"IL\",\"countycode\":\"\",\"uzip\":\"\",\"hash\":\"\",\"woeid\":2379574,\"woetype\":7}]}}");
    expect(resource.findLocation("Beer and Wine, Chicago, IL")).andReturn(returnCode);
    replayAll();
    Location location = yahooGeolocator.locate("Beer and Wine, Chicago, IL",
        GeolocationGranularity.BROAD);
    assertNull(location);
    verifyAll();
  }

  @Test
  public void testIntersectionShouldReturnResult() throws Exception {
    JSONObject returnCode = new JSONObject(
        "{\"ResultSet\":{\"version\":\"1.0\",\"Error\":0,\"ErrorMessage\":\"No error\",\"Locale\":\"us_US\",\"Quality\":87,\"Found\":1,\"Results\":[{\"quality\":80,\"latitude\":\"41.880730\",\"longitude\":\"-87.629379\",\"offsetlat\":\"41.880730\",\"offsetlon\":\"-87.629379\",\"radius\":500,\"name\":\"\",\"line1\":\"S Dearborn St & W Monroe St\",\"line2\":\"Chicago, IL  60603\",\"line3\":\"\",\"line4\":\"United States\",\"house\":null,\"street\":\"S Dearborn St\",\"xstreet\":\"W Monroe St\",\"unittype\":\"\",\"unit\":\"\",\"postal\":\"60603\",\"neighborhood\":\"\",\"city\":\"Chicago\",\"county\":\"Cook County\",\"state\":\"Illinois\",\"country\":\"United States\",\"countrycode\":\"US\",\"statecode\":\"IL\",\"countycode\":\"\",\"uzip\":\"60603\",\"hash\":\"\",\"woeid\":12784257,\"woetype\":11}]}}");
    expect(resource.findLocation("Dearborn and Monroe, Chicago, IL")).andReturn(returnCode);
    replayAll();
    Location location = yahooGeolocator.locate("Dearborn and Monroe, Chicago, IL",
        GeolocationGranularity.BROAD);
    assertEquals(new Location(41.880730, -87.629379, "Dearborn and Monroe, Chicago, IL"), location);
    verifyAll();
  }
}
