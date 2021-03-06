package foodtruck.geolocation;


import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.appengine.cache.MemcacheCacher;
import foodtruck.model.Location;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette@gmail.com
 * @since 8/30/11
 */
@RunWith(MockitoJUnitRunner.class)
public class GoogleGeolocatorTest extends Mockito {
  @Mock private GoogleResource resource;
  private GoogleGeolocator geoLocator;
  @Mock private MemcacheCacher cacher;

  @Before
  public void before() {
    geoLocator = new GoogleGeolocator(resource, cacher);
  }

  @Test
  public void testLocate_ParseLatLong() {
    Location location = geoLocator.locate("-3.4343,87.634024", GeolocationGranularity.BROAD);
    assertThat(location).isNotNull();
    assertThat(location.getLatitude()).isWithin(0).of(-3.4343);
    assertThat(location.getLongitude()).isWithin(0).of(87.634024);
    assertThat(location.getName()).isNull();
  }

  @Test
  public void testLocate_ParseLatLong2() {
    Location location = geoLocator.locate("-3.4343,87.634024,Blah Blah", GeolocationGranularity.BROAD);
    assertThat(location).isNotNull();
    assertThat(location.getLatitude()).isWithin(0).of(-3.4343);
    assertThat(location.getLongitude()).isWithin(0).of(87.634024);
    assertThat(location.getName()).isEqualTo("Blah Blah");
  }

  @Test
  public void testLocate_ParseLatLong3() {
    Location location = geoLocator.locate("  -3, 87.634024, Blah Blah", GeolocationGranularity.BROAD);
    assertThat(location).isNotNull();
    assertThat(location.getLatitude()).isWithin(0).of(-3);
    assertThat(location.getLongitude()).isWithin(0).of(87.634024);
    assertThat(location.getName()).isEqualTo("Blah Blah");
  }

  @Test
  public void testLocate_ParseLatLong4() {
    Location location = geoLocator.parseLatLong("123. Main Street, Chicago, IL 60606");
    assertThat(location).isNull();
  }

  @Test
  public void testReverseLookup() throws JSONException {
    String response = "{\n" +
        "   \"results\" : [\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"285\",\n" +
        "               \"short_name\" : \"285\",\n" +
        "               \"types\" : [ \"street_number\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Bedford Ave\",\n" +
        "               \"short_name\" : \"Bedford Ave\",\n" +
        "               \"types\" : [ \"route\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Williamsburg\",\n" +
        "               \"short_name\" : \"Williamsburg\",\n" +
        "               \"types\" : [ \"neighborhood\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Brooklyn\",\n" +
        "               \"short_name\" : \"Brooklyn\",\n" +
        "               \"types\" : [ \"sublocality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"New York\",\n" +
        "               \"types\" : [ \"locality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Kings\",\n" +
        "               \"short_name\" : \"Kings\",\n" +
        "               \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"NY\",\n" +
        "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"11211\",\n" +
        "               \"short_name\" : \"11211\",\n" +
        "               \"types\" : [ \"postal_code\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"285 Bedford Ave, Brooklyn, NY 11211, USA\",\n" +
        "         \"geometry\" : {\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 40.71412890,\n" +
        "               \"lng\" : -73.96140740\n" +
        "            },\n" +
        "            \"location_type\" : \"ROOFTOP\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.71547788029149,\n" +
        "                  \"lng\" : -73.96005841970849\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.71277991970850,\n" +
        "                  \"lng\" : -73.96275638029151\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"types\" : [ \"street_address\" ]\n" +
        "      },\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"Grand St - Bedford Av\",\n" +
        "               \"short_name\" : \"Grand St - Bedford Av\",\n" +
        "               \"types\" : [ \"bus_station\", \"transit_station\", \"establishment\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Williamsburg\",\n" +
        "               \"short_name\" : \"Williamsburg\",\n" +
        "               \"types\" : [ \"neighborhood\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Brooklyn\",\n" +
        "               \"short_name\" : \"Brooklyn\",\n" +
        "               \"types\" : [ \"sublocality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Kings\",\n" +
        "               \"short_name\" : \"Kings\",\n" +
        "               \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"NY\",\n" +
        "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"11211\",\n" +
        "               \"short_name\" : \"11211\",\n" +
        "               \"types\" : [ \"postal_code\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"Grand St - Bedford Av, Brooklyn, NY 11211, USA\",\n" +
        "         \"geometry\" : {\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 40.7143210,\n" +
        "               \"lng\" : -73.9611510\n" +
        "            },\n" +
        "            \"location_type\" : \"APPROXIMATE\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.71566998029149,\n" +
        "                  \"lng\" : -73.95980201970849\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.71297201970850,\n" +
        "                  \"lng\" : -73.96249998029151\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"types\" : [ \"bus_station\", \"transit_station\", \"establishment\" ]\n" +
        "      },\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"Grand St - Bedford Av\",\n" +
        "               \"short_name\" : \"Grand St - Bedford Av\",\n" +
        "               \"types\" : [ \"bus_station\", \"transit_station\", \"establishment\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Williamsburg\",\n" +
        "               \"short_name\" : \"Williamsburg\",\n" +
        "               \"types\" : [ \"neighborhood\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Brooklyn\",\n" +
        "               \"short_name\" : \"Brooklyn\",\n" +
        "               \"types\" : [ \"sublocality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Kings\",\n" +
        "               \"short_name\" : \"Kings\",\n" +
        "               \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"NY\",\n" +
        "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"11211\",\n" +
        "               \"short_name\" : \"11211\",\n" +
        "               \"types\" : [ \"postal_code\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"Grand St - Bedford Av, Brooklyn, NY 11211, USA\",\n" +
        "         \"geometry\" : {\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 40.7146840,\n" +
        "               \"lng\" : -73.9615630\n" +
        "            },\n" +
        "            \"location_type\" : \"APPROXIMATE\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.71603298029149,\n" +
        "                  \"lng\" : -73.96021401970850\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.71333501970850,\n" +
        "                  \"lng\" : -73.96291198029151\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"types\" : [ \"bus_station\", \"transit_station\", \"establishment\" ]\n" +
        "      },\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"Bedford Av - Grand St\",\n" +
        "               \"short_name\" : \"Bedford Av - Grand St\",\n" +
        "               \"types\" : [ \"bus_station\", \"transit_station\", \"establishment\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Williamsburg\",\n" +
        "               \"short_name\" : \"Williamsburg\",\n" +
        "               \"types\" : [ \"neighborhood\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Brooklyn\",\n" +
        "               \"short_name\" : \"Brooklyn\",\n" +
        "               \"types\" : [ \"sublocality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Kings\",\n" +
        "               \"short_name\" : \"Kings\",\n" +
        "               \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"NY\",\n" +
        "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"11211\",\n" +
        "               \"short_name\" : \"11211\",\n" +
        "               \"types\" : [ \"postal_code\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"Bedford Av - Grand St, Brooklyn, NY 11211, USA\",\n" +
        "         \"geometry\" : {\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 40.714710,\n" +
        "               \"lng\" : -73.9609990\n" +
        "            },\n" +
        "            \"location_type\" : \"APPROXIMATE\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.71605898029150,\n" +
        "                  \"lng\" : -73.95965001970849\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.71336101970850,\n" +
        "                  \"lng\" : -73.96234798029150\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"types\" : [ \"bus_station\", \"transit_station\", \"establishment\" ]\n" +
        "      },\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"11249\",\n" +
        "               \"short_name\" : \"11249\",\n" +
        "               \"types\" : [ \"postal_code\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Brooklyn\",\n" +
        "               \"short_name\" : \"Brooklyn\",\n" +
        "               \"types\" : [ \"sublocality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"New York\",\n" +
        "               \"types\" : [ \"locality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"NY\",\n" +
        "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"Brooklyn, NY 11249, USA\",\n" +
        "         \"geometry\" : {\n" +
        "            \"bounds\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.72539970,\n" +
        "                  \"lng\" : -73.95508570\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.70790830000001,\n" +
        "                  \"lng\" : -73.96977950\n" +
        "               }\n" +
        "            },\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 40.7175320,\n" +
        "               \"lng\" : -73.9612350\n" +
        "            },\n" +
        "            \"location_type\" : \"APPROXIMATE\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.72539970,\n" +
        "                  \"lng\" : -73.95508570\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.70790830000001,\n" +
        "                  \"lng\" : -73.96977950\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"types\" : [ \"postal_code\" ]\n" +
        "      },\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"Williamsburg\",\n" +
        "               \"short_name\" : \"Williamsburg\",\n" +
        "               \"types\" : [ \"neighborhood\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Manhattan\",\n" +
        "               \"short_name\" : \"Manhattan\",\n" +
        "               \"types\" : [ \"sublocality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"New York\",\n" +
        "               \"types\" : [ \"locality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Kings\",\n" +
        "               \"short_name\" : \"Kings\",\n" +
        "               \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"NY\",\n" +
        "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"Williamsburg, Manhattan, NY, USA\",\n" +
        "         \"geometry\" : {\n" +
        "            \"bounds\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.72517730,\n" +
        "                  \"lng\" : -73.9364980\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.6979330,\n" +
        "                  \"lng\" : -73.96984510\n" +
        "               }\n" +
        "            },\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 40.70644610,\n" +
        "               \"lng\" : -73.95361629999999\n" +
        "            },\n" +
        "            \"location_type\" : \"APPROXIMATE\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.72517730,\n" +
        "                  \"lng\" : -73.9364980\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.6979330,\n" +
        "                  \"lng\" : -73.96984510\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"types\" : [ \"neighborhood\", \"political\" ]\n" +
        "      },\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"11211\",\n" +
        "               \"short_name\" : \"11211\",\n" +
        "               \"types\" : [ \"postal_code\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Brooklyn\",\n" +
        "               \"short_name\" : \"Brooklyn\",\n" +
        "               \"types\" : [ \"sublocality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"New York\",\n" +
        "               \"types\" : [ \"locality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"NY\",\n" +
        "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"Brooklyn, NY 11211, USA\",\n" +
        "         \"geometry\" : {\n" +
        "            \"bounds\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.7280090,\n" +
        "                  \"lng\" : -73.92072990\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.69763590,\n" +
        "                  \"lng\" : -73.97616690\n" +
        "               }\n" +
        "            },\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 40.71800360,\n" +
        "               \"lng\" : -73.96537150000002\n" +
        "            },\n" +
        "            \"location_type\" : \"APPROXIMATE\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.7280090,\n" +
        "                  \"lng\" : -73.92072990\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.69763590,\n" +
        "                  \"lng\" : -73.97616690\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"types\" : [ \"postal_code\" ]\n" +
        "      },\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"Kings\",\n" +
        "               \"short_name\" : \"Kings\",\n" +
        "               \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"NY\",\n" +
        "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"Kings, NY, USA\",\n" +
        "         \"geometry\" : {\n" +
        "            \"bounds\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.7394460,\n" +
        "                  \"lng\" : -73.8333650\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.57047340,\n" +
        "                  \"lng\" : -74.04200489999999\n" +
        "               }\n" +
        "            },\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 40.65287620,\n" +
        "               \"lng\" : -73.95949399999999\n" +
        "            },\n" +
        "            \"location_type\" : \"APPROXIMATE\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.7394460,\n" +
        "                  \"lng\" : -73.8333650\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.57047340,\n" +
        "                  \"lng\" : -74.04200489999999\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
        "      },\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"Brooklyn\",\n" +
        "               \"short_name\" : \"Brooklyn\",\n" +
        "               \"types\" : [ \"sublocality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"New York\",\n" +
        "               \"types\" : [ \"locality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"Kings\",\n" +
        "               \"short_name\" : \"Kings\",\n" +
        "               \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"NY\",\n" +
        "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"Brooklyn, NY, USA\",\n" +
        "         \"geometry\" : {\n" +
        "            \"bounds\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.7394460,\n" +
        "                  \"lng\" : -73.8333650\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.55104190,\n" +
        "                  \"lng\" : -74.056630\n" +
        "               }\n" +
        "            },\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 40.650,\n" +
        "               \"lng\" : -73.950\n" +
        "            },\n" +
        "            \"location_type\" : \"APPROXIMATE\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.7394460,\n" +
        "                  \"lng\" : -73.8333650\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.55104190,\n" +
        "                  \"lng\" : -74.056630\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"types\" : [ \"sublocality\", \"political\" ]\n" +
        "      },\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"New York\",\n" +
        "               \"types\" : [ \"locality\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"New York\",\n" +
        "               \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"NY\",\n" +
        "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"New York, NY, USA\",\n" +
        "         \"geometry\" : {\n" +
        "            \"bounds\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.91524130,\n" +
        "                  \"lng\" : -73.7002720\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.4959080,\n" +
        "                  \"lng\" : -74.25908790\n" +
        "               }\n" +
        "            },\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 40.71435280,\n" +
        "               \"lng\" : -74.00597309999999\n" +
        "            },\n" +
        "            \"location_type\" : \"APPROXIMATE\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 40.91524130,\n" +
        "                  \"lng\" : -73.7002720\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.4959080,\n" +
        "                  \"lng\" : -74.25908790\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"types\" : [ \"locality\", \"political\" ]\n" +
        "      },\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"New York\",\n" +
        "               \"short_name\" : \"NY\",\n" +
        "               \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "            },\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"New York, USA\",\n" +
        "         \"geometry\" : {\n" +
        "            \"bounds\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 45.0158650,\n" +
        "                  \"lng\" : -71.85626990\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.49594540,\n" +
        "                  \"lng\" : -79.76214390\n" +
        "               }\n" +
        "            },\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 43.29942850,\n" +
        "               \"lng\" : -74.21793260000001\n" +
        "            },\n" +
        "            \"location_type\" : \"APPROXIMATE\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 45.0158650,\n" +
        "                  \"lng\" : -71.85626990\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : 40.49594540,\n" +
        "                  \"lng\" : -79.76214390\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
        "      },\n" +
        "      {\n" +
        "         \"address_components\" : [\n" +
        "            {\n" +
        "               \"long_name\" : \"United States\",\n" +
        "               \"short_name\" : \"US\",\n" +
        "               \"types\" : [ \"country\", \"political\" ]\n" +
        "            }\n" +
        "         ],\n" +
        "         \"formatted_address\" : \"United States\",\n" +
        "         \"geometry\" : {\n" +
        "            \"bounds\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 90.0,\n" +
        "                  \"lng\" : 180.0\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : -90.0,\n" +
        "                  \"lng\" : -180.0\n" +
        "               }\n" +
        "            },\n" +
        "            \"location\" : {\n" +
        "               \"lat\" : 37.090240,\n" +
        "               \"lng\" : -95.7128910\n" +
        "            },\n" +
        "            \"location_type\" : \"APPROXIMATE\",\n" +
        "            \"viewport\" : {\n" +
        "               \"northeast\" : {\n" +
        "                  \"lat\" : 90.0,\n" +
        "                  \"lng\" : 180.0\n" +
        "               },\n" +
        "               \"southwest\" : {\n" +
        "                  \"lat\" : -90.0,\n" +
        "                  \"lng\" : -180.0\n" +
        "               }\n" +
        "            }\n" +
        "         },\n" +
        "         \"types\" : [ \"country\", \"political\" ]\n" +
        "      }\n" +
        "   ],\n" +
        "   \"status\" : \"OK\"\n" +
        "}";

    Location location = Location.builder().lat(40.714224).lng(-73.961452).build();
    when(resource.reverseLookup(location)).thenReturn(new JSONObject(response));
    final Location actual = geoLocator.reverseLookup(location);
    assertThat(actual.getName()).isEqualTo("285 Bedford Ave, Brooklyn, NY 11211, USA");
    verify(resource).reverseLookup(location);
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
    when(resource.findLocation(thelocation)).thenReturn(response);
    Location location = geoLocator.locate(thelocation, GeolocationGranularity.BROAD);
    assertThat(location).isNull();
    verify(resource).findLocation(thelocation);
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
    when(resource.findLocation("Dearborn and Monroe, Chicago, IL")).thenReturn(response);
    Location location = geoLocator.locate("Dearborn and Monroe, Chicago, IL",
        GeolocationGranularity.BROAD);
    assertThat(location).isEqualTo(Location.builder().lat(41.8807438).lng(-87.6293867).name("Dearborn and Monroe, Chicago, IL")
        .build());
  }

  @Test
  public void testPointOfInterest() throws JSONException {
    String jsonString = "{\"results\":[{\"address_components\":[{\"long_name\":\"Skydeck Chicago\",\"short_name\":\"Skydeck Chicago\",\"types\":[\"point_of_interest\",\"establishment\"]},{\"long_name\":\"233\",\"short_name\":\"233\",\"types\":[\"street_number\"]},{\"long_name\":\"South Wacker Drive\",\"short_name\":\"S Wacker Dr\",\"types\":[\"route\"]},{\"long_name\":\"Loop\",\"short_name\":\"Loop\",\"types\":[\"neighborhood\",\"political\"]},{\"long_name\":\"Chicago\",\"short_name\":\"Chicago\",\"types\":[\"locality\",\"political\"]},{\"long_name\":\"Cook\",\"short_name\":\"Cook\",\"types\":[\"administrative_area_level_2\",\"political\"]},{\"long_name\":\"Illinois\",\"short_name\":\"IL\",\"types\":[\"administrative_area_level_1\",\"political\"]},{\"long_name\":\"United States\",\"short_name\":\"US\",\"types\":[\"country\",\"political\"]},{\"long_name\":\"60606\",\"short_name\":\"60606\",\"types\":[\"postal_code\"]},{\"long_name\":\"6437\",\"short_name\":\"6437\",\"types\":[]}],\"formatted_address\":\"Skydeck Chicago, 233 South Wacker Drive, Chicago, IL 60606, USA\",\"geometry\":{\"location\":{\"lat\":41.8788918,\"lng\":-87.6358151},\"location_type\":\"APPROXIMATE\",\"viewport\":{\"northeast\":{\"lat\":41.887199,\"lng\":-87.6198077},\"southwest\":{\"lat\":41.8705835,\"lng\":-87.6518225}}},\"partial_match\":true,\"types\":[\"point_of_interest\",\"museum\",\"establishment\"]}],\"status\":\"OK\"}\n";
    JSONObject response = new JSONObject(jsonString);
    when(resource.findLocation("Willis Tower")).thenReturn(response);
    Location location = geoLocator.locate("Willis Tower",
        GeolocationGranularity.BROAD);
    assertThat(location).isEqualTo(Location.builder().lat(41.878891).lng(-87.635815).name("Willis Tower")
        .build());
  }

  @Test(expected = OverQueryLimitException.class)
  public void testReverseLookupOverQueryLimit() throws JSONException {
    Location location = Location.builder().lat(-123).lng(456).build();
    JSONObject response = new JSONObject("{\"results\":[],\"status\":\"OVER_QUERY_LIMIT\"}");
    when(resource.reverseLookup(location)).thenReturn(response);
    geoLocator.reverseLookup(location);
  }

  @Test
  public void testOverlyBroad() throws JSONException {
    String jsonText = "{\n" +
        "    \"results\": [\n" +
        "        {\n" +
        "            \"address_components\": [\n" +
        "                {\n" +
        "                    \"long_name\": \"Sculpture \\\"Monument to the Great Northern Migration\\\"\", \n" +
        "                    \"short_name\": \"Sculpture \\\"Monument to the Great Northern Migration\\\"\", \n" +
        "                    \"types\": [\n" +
        "                        \"point_of_interest\", \n" +
        "                        \"establishment\"\n" +
        "                    ]\n" +
        "                }, \n" +
        "                {\n" +
        "                    \"long_name\": \"South Doctor Martin Luther King Junior Drive\", \n" +
        "                    \"short_name\": \"S Martin Luther King Dr\", \n" +
        "                    \"types\": [\n" +
        "                        \"route\"\n" +
        "                    ]\n" +
        "                }, \n" +
        "                {\n" +
        "                    \"long_name\": \"Near South Side\", \n" +
        "                    \"short_name\": \"Near South Side\", \n" +
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
        "                    \"long_name\": \"60616\", \n" +
        "                    \"short_name\": \"60616\", \n" +
        "                    \"types\": [\n" +
        "                        \"postal_code\"\n" +
        "                    ]\n" +
        "                }\n" +
        "            ], \n" +
        "            \"formatted_address\": \"Sculpture \\\"Monument to the Great Northern Migration\\\", South Doctor Martin Luther King Junior Drive, Chicago, IL 60616, USA\", \n" +
        "            \"geometry\": {\n" +
        "                \"location\": {\n" +
        "                    \"lat\": 41.8468682, \n" +
        "                    \"lng\": -87.61754959999999\n" +
        "                }, \n" +
        "                \"location_type\": \"APPROXIMATE\", \n" +
        "                \"viewport\": {\n" +
        "                    \"northeast\": {\n" +
        "                        \"lat\": 41.84821718029149, \n" +
        "                        \"lng\": -87.61620061970848\n" +
        "                    }, \n" +
        "                    \"southwest\": {\n" +
        "                        \"lat\": 41.8455192197085, \n" +
        "                        \"lng\": -87.61889858029149\n" +
        "                    }\n" +
        "                }\n" +
        "            }, \n" +
        "            \"partial_match\": true, \n" +
        "            \"types\": [\n" +
        "                \"point_of_interest\", \n" +
        "                \"establishment\"\n" +
        "            ]\n" +
        "        }\n" +
        "    ], \n" +
        "    \"status\": \"OK\"\n" +
        "}\n";
    JSONObject response = new JSONObject(jsonText);
    when(resource.findLocation("Willis Tower")).thenReturn(response);
    Location location = geoLocator.locate("Willis Tower",
        GeolocationGranularity.NARROW);
    assertThat(location).isNull();
  }
}
