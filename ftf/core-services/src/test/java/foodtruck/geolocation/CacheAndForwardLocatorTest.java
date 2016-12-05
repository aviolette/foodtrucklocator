package foodtruck.geolocation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import foodtruck.monitoring.CounterPublisher;
import foodtruck.time.Clock;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author aviolette@gmail.com
 * @since 9/8/11
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheAndForwardLocatorTest {
  private final static String LOCATION_NAME = "Location";
  private @Mock LocationDAO dao;
  private @Mock GeoLocator secondary;
  private @Mock Clock clock;
  private @Mock CounterPublisher countPublisher;
  private CacheAndForwardLocator locator;
  private Location namedLocation;


  @Before
  public void before() {
    locator = new CacheAndForwardLocator(dao, secondary, countPublisher);
    namedLocation = Location.builder().lat(-3).lng(-4).name(LOCATION_NAME).build();
  }

  @Test
  public void shouldReturnCachedLocationIfFound() {
    monitorUpdate();
    when(dao.findByAlias(LOCATION_NAME)).thenReturn(namedLocation);
    Location loc = locator.locate(LOCATION_NAME, GeolocationGranularity.BROAD);
    assertThat(loc).isEqualTo(namedLocation);
  }

  private void monitorUpdate() {
    countPublisher.increment("cacheLookup_total");
  }

  @Test
  public void shouldPerformGeoLookupAndSaveInCacheIfNotFoundInCache() {
    monitorUpdate();
    countPublisher.increment("cacheLookup_failed");
    when(dao.findByAlias(LOCATION_NAME)).thenReturn(null);
    when(secondary.locate(LOCATION_NAME, GeolocationGranularity.BROAD)).thenReturn(namedLocation);
    when(dao.saveAndFetch(namedLocation)).thenReturn(namedLocation);
    Location loc = locator.locate(LOCATION_NAME, GeolocationGranularity.BROAD);
    assertThat(loc).isEqualTo(namedLocation);
  }

  @Test
  public void shouldReturnNonResolvedWhenNotFound() {
    monitorUpdate();
    countPublisher.increment("cacheLookup_failed");
    when(dao.findByAlias(LOCATION_NAME)).thenReturn(null);
    when(secondary.locate(LOCATION_NAME, GeolocationGranularity.BROAD)).thenReturn(null);
    Location targetLoc = Location.builder().name(LOCATION_NAME).valid(false).build();
    when(dao.saveAndFetch(targetLoc)).thenReturn(targetLoc);
    Location loc = locator.locate(LOCATION_NAME, GeolocationGranularity.BROAD);
    assertThat(loc.isResolved()).isFalse();
  }
}
