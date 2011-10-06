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
    namedLocation = new Location(-3, -4, LOCATION_NAME);
    unnamedLocation = new Location(-4, -5, null);
  }

  @Test
  public void shouldReturnCachedLocationIfFound() {
    expect(dao.lookup(LOCATION_NAME)).andReturn(namedLocation);
    replayAll();
    Location loc = locator.locate(LOCATION_NAME);
    assertEquals(loc, namedLocation);
    verifyAll();
  }

  @Test
  public void shouldPerformGeoLookupAndSaveInCacheIfNotFoundInCache() {
    expect(dao.lookup(LOCATION_NAME)).andReturn(null);
    expect(secondary.locate(LOCATION_NAME)).andReturn(namedLocation);
    dao.save(namedLocation);
    replayAll();
    Location loc = locator.locate(LOCATION_NAME);
    assertEquals(loc, namedLocation);
    verifyAll();
  }

  @Test
  public void shouldReturnNullWhenNotFound() {
    expect(dao.lookup(LOCATION_NAME)).andReturn(null);
    expect(secondary.locate(LOCATION_NAME)).andReturn(null);
    dao.saveAttemptFailed(LOCATION_NAME);
    replayAll();
    Location loc = locator.locate(LOCATION_NAME);
    assertNull(loc);
    verifyAll();
  }
}
