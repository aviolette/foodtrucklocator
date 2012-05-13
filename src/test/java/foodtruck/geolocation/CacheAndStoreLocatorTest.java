package foodtruck.geolocation;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author aviolette@gmail.com
 * @since 9/8/11
 */
public class CacheAndStoreLocatorTest extends EasyMockSupport {
  private LocationDAO dao;
  private CacheAndStoreLocator locator;
  private GeoLocator secondary;
  private final static String LOCATION_NAME = "Location";
  private Location unnamedLocation;
  private Location namedLocation;

  @Before
  public void before() {
    dao = createMock(LocationDAO.class);
    secondary = createMock(GeoLocator.class);
    locator = new CacheAndStoreLocator(dao, secondary);
    namedLocation = Location.builder().lat(-3).lng(-4).name(LOCATION_NAME).build();
    unnamedLocation = Location.builder().lat(-4).lng(-5).build();
  }

  @Test
  public void shouldReturnCachedLocationIfFound() {
    expect(dao.findByAddress(LOCATION_NAME)).andReturn(namedLocation);
    replayAll();
    Location loc = locator.locate(LOCATION_NAME, GeolocationGranularity.BROAD);
    assertEquals(loc, namedLocation);
    verifyAll();
  }

  @Test
  public void shouldPerformGeoLookupAndSaveInCacheIfNotFoundInCache() {
    expect(dao.findByAddress(LOCATION_NAME)).andReturn(null);
    expect(secondary.locate(LOCATION_NAME, GeolocationGranularity.BROAD)).andReturn(namedLocation);
    expect(dao.saveAndFetch(namedLocation)).andReturn(namedLocation);
    replayAll();
    Location loc = locator.locate(LOCATION_NAME, GeolocationGranularity.BROAD);
    assertEquals(loc, namedLocation);
    verifyAll();
  }

  @Test
  public void shouldReturnNullWhenNotFound() {
    expect(dao.findByAddress(LOCATION_NAME)).andReturn(null);
    expect(secondary.locate(LOCATION_NAME, GeolocationGranularity.BROAD)).andReturn(null);
    dao.saveAttemptFailed(LOCATION_NAME);
    replayAll();
    Location loc = locator.locate(LOCATION_NAME, GeolocationGranularity.BROAD);
    assertNull(loc);
    verifyAll();
  }
}
