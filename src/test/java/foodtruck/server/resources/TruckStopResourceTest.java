package foodtruck.server.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sun.jersey.api.JResponse;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckLocationGroup;
import foodtruck.server.security.SecurityChecker;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
public class TruckStopResourceTest extends EasyMockSupport {
  private FoodTruckStopService service;
  private Clock clock;
  private TruckStopResource truckStopResource;
  private TruckLocationGroup truckStopGroup1;
  private TruckLocationGroup truckStopGroup2;
  private SecurityChecker checker;

  @Before
  public void setup() {
    service = createMock(FoodTruckStopService.class);
    clock = createMock(Clock.class);
    checker = createMock(SecurityChecker.class);
    truckStopGroup1 = new TruckLocationGroup(null, Sets.<Truck>newHashSet());
    truckStopGroup2 = new TruckLocationGroup(new Location.Builder().name("foo").build(),
        Sets.<Truck>newHashSet());
    truckStopResource = new TruckStopResource(service, clock, checker);
  }

  @Test
  public void getStopsNoTimeSpecified() {
    DateTime dateTime = new DateTime();
    expect(clock.now()).andReturn(dateTime);
    expect(service.findFoodTruckGroups(dateTime)).andReturn(
        ImmutableSet.<TruckLocationGroup>of(truckStopGroup1, truckStopGroup2));
    replayAll();
    JResponse response = truckStopResource.getStops(null);
    assertEquals(response.getEntity(), ImmutableSet.of(truckStopGroup2));
    assertEquals("no-cache", response.getMetadata().getFirst("Cache-Control"));
    assertEquals("Thu, 01 Jan 1970 00:00:00 GMT", response.getMetadata().getFirst("Expires"));
    assertEquals("no-cache", response.getMetadata().getFirst("Pragma"));
    verifyAll();
  }

  @Test
  public void getStopsTimeSpecified() {
    DateTime dateTime = new DateTime();
    expect(service.findFoodTruckGroups(dateTime)).andReturn(
        ImmutableSet.<TruckLocationGroup>of(truckStopGroup1, truckStopGroup2));
    replayAll();
    JResponse response = truckStopResource.getStops(dateTime);
    assertEquals(response.getEntity(), ImmutableSet.of(truckStopGroup2));
    verifyAll();
  }
}