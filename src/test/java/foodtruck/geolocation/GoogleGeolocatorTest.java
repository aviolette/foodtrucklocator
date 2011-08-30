package foodtruck.geolocation;

import com.sun.jersey.api.client.WebResource;

import org.junit.Before;
import org.junit.Test;

import foodtruck.model.Location;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author aviolette@gmail.com
 * @since 8/30/11
 */
public class GoogleGeolocatorTest {
  private GoogleGeolocator geoLocator;
  private WebResource resource;

  @Before
  public void before() {
    geoLocator = new GoogleGeolocator(resource);
  }

  @Test
  public void testLocate_ParseLatLong() {
    Location location = geoLocator.locate("-3.4343,343444");
    assertNotNull(location);
    assertEquals(-3.4343, location.getLatitude(), 0);
    assertEquals(343444.0, location.getLongitude(), 0);
    assertNull(location.getName());
  }

  @Test
  public void testLocate_ParseLatLong2() {
    Location location = geoLocator.locate("-3.4343,343444,Blah Blah");
    assertNotNull(location);
    assertEquals(-3.4343, location.getLatitude(), 0);
    assertEquals(343444.0, location.getLongitude(), 0);
    assertEquals("Blah Blah", location.getName());
  }

  @Test
  public void testLocate_ParseLatLong3() {
    Location location = geoLocator.locate("  -3, 343444, Blah Blah");
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
}
