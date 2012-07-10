package foodtruck.geolocation;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.SystemStatDAO;
import foodtruck.model.Location;
import foodtruck.util.Clock;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
  private Clock clock;
  private SystemStatDAO monitor;
  private DateTime now;

  @Before
  public void before() {
    dao = createMock(LocationDAO.class);
    secondary = createMock(GeoLocator.class);
    monitor = createMock(SystemStatDAO.class);
    clock = createMock(Clock.class);
    now = new DateTime();
    expect(clock.now()).andStubReturn(now);
    locator = new CacheAndStoreLocator(dao, secondary, monitor, clock);
    namedLocation = Location.builder().lat(-3).lng(-4).name(LOCATION_NAME).build();
    unnamedLocation = Location.builder().lat(-4).lng(-5).build();
  }

  @Test
  public void shouldReturnCachedLocationIfFound() {
    monitorUpdate();
    expect(dao.findByAddress(LOCATION_NAME)).andReturn(namedLocation);
    replayAll();
    Location loc = locator.locate(LOCATION_NAME, GeolocationGranularity.BROAD);
    assertEquals(loc, namedLocation);
    verifyAll();
  }

  private void monitorUpdate() {
    monitor.updateCount(now, "cacheLookup_total");
  }

  @Test
  public void shouldPerformGeoLookupAndSaveInCacheIfNotFoundInCache() {
    monitorUpdate();
    monitor.updateCount(now, "cacheLookup_failed");
    expect(dao.findByAddress(LOCATION_NAME)).andReturn(null);
    expect(secondary.locate(LOCATION_NAME, GeolocationGranularity.BROAD)).andReturn(namedLocation);
    expect(dao.saveAndFetch(namedLocation)).andReturn(namedLocation);
    replayAll();
    Location loc = locator.locate(LOCATION_NAME, GeolocationGranularity.BROAD);
    assertEquals(loc, namedLocation);
    verifyAll();
  }

  @Test
  public void shouldReturnNonResolvedWhenNotFound() {
    monitorUpdate();
    monitor.updateCount(now, "cacheLookup_failed");
    expect(dao.findByAddress(LOCATION_NAME)).andReturn(null);
    expect(secondary.locate(LOCATION_NAME, GeolocationGranularity.BROAD)).andReturn(null);
    Location targetLoc = Location.builder().name(LOCATION_NAME).valid(false).build();
    expect(dao.saveAndFetch(targetLoc)).andReturn(targetLoc);
    replayAll();
    Location loc = locator.locate(LOCATION_NAME, GeolocationGranularity.BROAD);
    assertFalse(loc.isResolved());
    verifyAll();
  }
}
