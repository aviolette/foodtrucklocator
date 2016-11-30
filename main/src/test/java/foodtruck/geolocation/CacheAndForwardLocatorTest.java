package foodtruck.geolocation;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import foodtruck.monitoring.CounterPublisher;
import foodtruck.time.Clock;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author aviolette@gmail.com
 * @since 9/8/11
 */
public class CacheAndForwardLocatorTest extends EasyMockSupport {
  private final static String LOCATION_NAME = "Location";
  private LocationDAO dao;
  private CacheAndForwardLocator locator;
  private GeoLocator secondary;
  private Location unnamedLocation;
  private Location namedLocation;
  private Clock clock;
  private DateTime now;
  private CounterPublisher countPublisher;

  @Before
  public void before() {
    dao = createMock(LocationDAO.class);
    secondary = createMock(GeoLocator.class);
    clock = createMock(Clock.class);
    now = new DateTime();
    expect(clock.now()).andStubReturn(now);
    countPublisher = createMock(CounterPublisher.class);
    locator = new CacheAndForwardLocator(dao, secondary, countPublisher);
    namedLocation = Location.builder().lat(-3).lng(-4).name(LOCATION_NAME).build();
    unnamedLocation = Location.builder().lat(-4).lng(-5).build();
  }

  @Test
  public void shouldReturnCachedLocationIfFound() {
    monitorUpdate();
    expect(dao.findByAlias(LOCATION_NAME)).andReturn(namedLocation);
    replayAll();
    Location loc = locator.locate(LOCATION_NAME, GeolocationGranularity.BROAD);
    assertEquals(loc, namedLocation);
    verifyAll();
  }

  private void monitorUpdate() {
    countPublisher.increment("cacheLookup_total");
  }

  @Test
  public void shouldPerformGeoLookupAndSaveInCacheIfNotFoundInCache() {
    monitorUpdate();
    countPublisher.increment("cacheLookup_failed");
    expect(dao.findByAlias(LOCATION_NAME)).andReturn(null);
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
    countPublisher.increment("cacheLookup_failed");
    expect(dao.findByAlias(LOCATION_NAME)).andReturn(null);
    expect(secondary.locate(LOCATION_NAME, GeolocationGranularity.BROAD)).andReturn(null);
    Location targetLoc = Location.builder().name(LOCATION_NAME).valid(false).build();
    expect(dao.saveAndFetch(targetLoc)).andReturn(targetLoc);
    replayAll();
    Location loc = locator.locate(LOCATION_NAME, GeolocationGranularity.BROAD);
    assertFalse(loc.isResolved());
    verifyAll();
  }
}
