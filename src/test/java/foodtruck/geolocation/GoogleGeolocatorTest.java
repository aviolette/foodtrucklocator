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
import static org.junit.Assert.assertNull;

/**
 * @author aviolette@gmail.com
 * @since 8/30/11
 */
public class GoogleGeolocatorTest extends EasyMockSupport {
  private GoogleGeolocator geoLocator;
  private GoogleResource resource;

  @Before
  public void before() {
    resource = createMock(GoogleResource.class);
    geoLocator = new GoogleGeolocator(resource);
  }

  @Test
  public void testLocate_ParseLatLong() {
    Location location = geoLocator.locate("-3.4343,343444", GeolocationGranularity.BROAD);
    assertNotNull(location);
    assertEquals(-3.4343, location.getLatitude(), 0);
    assertEquals(343444.0, location.getLongitude(), 0);
    assertNull(location.getName());
  }

  @Test
  public void testLocate_ParseLatLong2() {
    Location location = geoLocator.locate("-3.4343,343444,Blah Blah", GeolocationGranularity.BROAD);
    assertNotNull(location);
    assertEquals(-3.4343, location.getLatitude(), 0);
    assertEquals(343444.0, location.getLongitude(), 0);
    assertEquals("Blah Blah", location.getName());
  }

  @Test
  public void testLocate_ParseLatLong3() {
    Location location = geoLocator.locate("  -3, 343444, Blah Blah", GeolocationGranularity.BROAD);
    assertNotNull(location);
    assertEquals(-3, location.getLatitude(), 0);
    assertEquals(343444.0, location.getLongitude(), 0);
    assertEquals("Blah Blah", location.getName());
  }

  @Test
  public void testLocate_ParseLatLong4() {
    Location location = geoLocator.parseLatLong("123. Main Street, Chicago, IL 60606");
    assertNull(location);
  }

  @Test
  public void testLocate_LocationTypeIsGEOMETRIC_CENTER() throws JSONException {
    String result = " {   \"results\": [\n" +
        "        {\n" +
        "            \"address_components\": [\n" +
        "                {\n" +
        "                    \"long_name\": \"Martin D'Arcy Museum of Art\", \n" +
        "                    \"short_name\": \"Martin D'Arcy Museum of Art\", \n" +
        "                    \"types\": [\n" +
        "                        \"point_of_interest\", \n" +
        "                        \"establishment\"\n" +
        "                    ]\n" +
        "                }, \n" +
        "                {\n" +
        "                    \"long_name\": \"820\", \n" +
        "                    \"short_name\": \"820\", \n" +
        "                    \"types\": [\n" +
        "                        \"street_number\"\n" +
        "                    ]\n" +
        "                }, \n" +
        "                {\n" +
        "                    \"long_name\": \"N Michigan Ave\", \n" +
        "                    \"short_name\": \"N Michigan Ave\", \n" +
        "                    \"types\": [\n" +
        "                        \"route\"\n" +
        "                    ]\n" +
        "                }, \n" +
        "                {\n" +
        "                    \"long_name\": \"Central Chicago\", \n" +
        "                    \"short_name\": \"Central Chicago\", \n" +
        "                    \"types\": [\n" +
        "                        \"neighborhood\", \n" +
        "                        \"political\"\n" +
        "                    ]\n" +
        "                }, \n" +
        "                {\n" +
        "                    \"long_name\": \"Chicago\", \n" +
        "                    \"short_name\": \"Chicago\", \n" +
        "                    \"types\": [\n" +
        "                        \"locality\", \n" +
        "                        \"political\"\n" +
        "                    ]\n" +
        "                }, \n" +
        "                {\n" +
        "                    \"long_name\": \"Cook\", \n" +
        "                    \"short_name\": \"Cook\", \n" +
        "                    \"types\": [\n" +
        "                        \"administrative_area_level_2\", \n" +
        "                        \"political\"\n" +
        "                    ]\n" +
        "                }, \n" +
        "                {\n" +
        "                    \"long_name\": \"Illinois\", \n" +
        "                    \"short_name\": \"IL\", \n" +
        "                    \"types\": [\n" +
        "                        \"administrative_area_level_1\", \n" +
        "                        \"political\"\n" +
        "                    ]\n" +
        "                }, \n" +
        "                {\n" +
        "                    \"long_name\": \"United States\", \n" +
        "                    \"short_name\": \"US\", \n" +
        "                    \"types\": [\n" +
        "                        \"country\", \n" +
        "                        \"political\"\n" +
        "                    ]\n" +
        "                }, \n" +
        "                {\n" +
        "                    \"long_name\": \"60611-2196\", \n" +
        "                    \"short_name\": \"60611-2196\", \n" +
        "                    \"types\": [\n" +
        "                        \"postal_code\"\n" +
        "                    ]\n" +
        "                }\n" +
        "            ], \n" +
        "            \"formatted_address\": \"Martin D'Arcy Museum of Art, 820 N Michigan Ave, Chicago, IL 60611-2196, USA\", \n" +
        "            \"geometry\": {\n" +
        "                \"location\": {\n" +
        "                    \"lat\": 41.897476500000003, \n" +
        "                    \"lng\": -87.625549699999993\n" +
        "                }, \n" +
        "                \"location_type\": \"GEOMETRIC_CENTER\", \n" +
        "                \"viewport\": {\n" +
        "                    \"northeast\": {\n" +
        "                        \"lat\": 41.905781300000001, \n" +
        "                        \"lng\": -87.609542300000001\n" +
        "                    }, \n" +
        "                    \"southwest\": {\n" +
        "                        \"lat\": 41.8891706, \n" +
        "                        \"lng\": -87.6415571\n" +
        "                    }\n" +
        "                }\n" +
        "            }, \n" +
        "            \"partial_match\": true, \n" +
        "            \"types\": [\n" +
        "                \"point_of_interest\", \n" +
        "                \"museum\", \n" +
        "                \"establishment\"\n" +
        "            ]\n" +
        "        }]}";
    JSONObject response = new JSONObject(result);
    final String thelocation = "Beer and Wine, Chicago, IL";
    expect(resource.findLocation(thelocation)).andReturn(response);
    replayAll();
    Location location = geoLocator.locate(thelocation, GeolocationGranularity.NARROW);
    assertNull(location);
    verifyAll();

  }

  @Test
  public void testLocate_City() throws JSONException {
    String city = "{\n" +
        "   \"results\" : [\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"Chicago\",\n" +
        "               \"short_name\" : \"Chicago\",\n" +
        "               \"types\" : [ \"locality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Cook\",\n" +
        "               \"short_name\" : \"Cook\",\n" +
        "               \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Illinois\",\n" +
        "               \"short_name\" : \"IL\",\n" +
        "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"Chicago, IL, USA\",\n" +
        "         \"geometry\" : {\n" +
        "            \"bounds\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 42.0231310,\n" +
        "                  \"lng\" : -87.52366090\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 41.6443350,\n" +
        "                  \"lng\" : -87.94026690\n" +
        "               }\n" +
        "            },\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 41.87811360,\n" +
        "               \"lng\" : -87.62979820\n" +
        "            },\n" +
        "            \"location_type\" : \"APPROXIMATE\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 42.01090120,\n" +
        "                  \"lng\" : -87.37367940\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 41.74504950,\n" +
        "                  \"lng\" : -87.88591699999999\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"types\" : [ \"locality\", \"political\" ]\n" +
        "      }\n" +
        "   ],\n" +
        "   \"status\" : \"OK\"\n" +
        "}";
    JSONObject response = new JSONObject(city);
    final String thelocation = "Beer and Wine, Chicago, IL";
    expect(resource.findLocation(thelocation)).andReturn(response);
    replayAll();
    Location location = geoLocator.locate(thelocation, GeolocationGranularity.BROAD);
    assertNull(location);
    verifyAll();

  }

  @Test
  public void testLocate_Intersection() throws JSONException {
    String intersection = "{\n" +
        "   \"results\" : [\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"W Monroe St\",\n" +
        "               \"short_name\" : \"W Monroe St\",\n" +
        "               \"types\" : [ \"route\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"The Loop\",\n" +
        "               \"short_name\" : \"The Loop\",\n" +
        "               \"types\" : [ \"neighborhood\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Chicago\",\n" +
        "               \"short_name\" : \"Chicago\",\n" +
        "               \"types\" : [ \"locality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Chicago\",\n" +
        "               \"short_name\" : \"Chicago\",\n" +
        "               \"types\" : [ \"administrative_area_level_3\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Cook\",\n" +
        "               \"short_name\" : \"Cook\",\n" +
        "               \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Illinois\",\n" +
        "               \"short_name\" : \"IL\",\n" +
        "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"60603\",\n" +
        "               \"short_name\" : \"60603\",\n" +
        "               \"types\" : [ \"postal_code\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"W Monroe St & S Dearborn St, Chicago, IL 60603, USA\",\n" +
        "         \"geometry\" : {\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 41.88074380,\n" +
        "               \"lng\" : -87.62938670\n" +
        "            },\n" +
        "            \"location_type\" : \"APPROXIMATE\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 41.88209278029149,\n" +
        "                  \"lng\" : -87.62803771970849\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 41.87939481970850,\n" +
        "                  \"lng\" : -87.63073568029149\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"partial_match\" : true,\n" +
        "         \"types\" : [ \"intersection\" ]\n" +
        "      }\n" +
        "   ],\n" +
        "   \"status\" : \"OK\"\n" +
        "}";
    JSONObject response = new JSONObject(intersection);
    expect(resource.findLocation("Dearborn and Monroe, Chicago, IL")).andReturn(response);
    replayAll();
    Location location = geoLocator.locate("Dearborn and Monroe, Chicago, IL",
        GeolocationGranularity.BROAD);
    assertEquals(Location.builder().lat(41.8807438).lng(-87.6293867).name("Dearborn and Monroe, Chicago, IL").build(),
        location);
    verifyAll();
  }
}
