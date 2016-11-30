package foodtruck.linxup;

import com.google.appengine.api.taskqueue.Queue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import com.javadocmd.simplelatlng.LatLng;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.dao.LinxupAccountDAO;
import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.model.LinxupAccount;
import foodtruck.model.Location;
import foodtruck.model.TrackingDevice;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.monitoring.Counter;
import foodtruck.server.security.SecurityChecker;
import foodtruck.util.Clock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author aviolette
 * @since 11/22/16
 */
@RunWith(MockitoJUnitRunner.class)
public class TrackingDeviceServiceImplTest {
  private static final String TRUCKID = "truckid";
  private static final String DEVICEID1 = "device1";
  private static final String DEVICELABEL1 = "label1";
  private static final long DEVICEDBID = 456L;
  @Mock private TruckStopDAO truckStopDAO;
  @Mock private LinxupConnector connector;
  @Mock private TrackingDeviceDAO trackingDeviceDAO;
  @Mock private GeoLocator locator;
  @Mock private Clock clock;
  @Mock private TruckDAO truckDAO;
  @Mock private DateTimeFormatter formatter;
  @Mock private SecurityChecker securityChecker;
  @Mock private LinxupAccountDAO linxupAccountDAO;
  @Mock private Provider<Queue> queueProvider;
  @Mock private TruckStopCache truckStopCache;
  @Mock private BlacklistedLocationMatcher blacklistLocationMatcher;
  @Mock private TrackingDeviceServiceImpl service;
  @Mock private LocationResolver locationResolver;
  @Mock private Counter errorCounter;
  private LinxupAccount account;
  private DateTime now = new DateTime(2016, 11, 22, 9, 0, 0);
  private Position position1;
  private TrackingDevice trackingDevice;
  private Location location1;
  private Truck truck;

  @Before
  public void before() {
    truck = Truck.builder()
        .id(TRUCKID)
        .name("The Truck")
        .build();
    position1 = Position.builder()
        .batteryCharge("12.3V")
        .date(now.minusMinutes(5))
        .deviceNumber(DEVICEID1)
        .direction(0)
        .driverId("Frank")
        .fuelLevel("73%")
        .latLng(new LatLng(40, 83.3))
        .speeding(false)
        .speedMph(14)
        .vehicleLabel(DEVICELABEL1)
        .build();
    account = LinxupAccount.builder()
        .enabled(true)
        .key(123L)
        .truckId(TRUCKID)
        .username("username")
        .password("password")
        .build();
    location1 = position1.toLocation();
    trackingDevice = TrackingDevice.builder()
        .deviceNumber(position1.getDeviceNumber())
        .lastLocation(location1)
        .lastActualLocation(position1.toLocation())
        .parked(false)
        .degreesFromNorth(position1.getDirection())
        .fuelLevel(position1.getFuelLevel())
        .batteryCharge(position1.getBatteryCharge())
        .atBlacklistedLocation(false)
        .truckOwnerId(TRUCKID)
        .lastBroadcast(position1.getDate())
        .label(position1.getVehicleLabel())
        .build();
    service = new TrackingDeviceServiceImpl(truckStopDAO, connector, trackingDeviceDAO, locator, clock, truckDAO,
        formatter, securityChecker, linxupAccountDAO, queueProvider, Providers.of(truckStopCache),
        blacklistLocationMatcher, locationResolver, errorCounter);
  }

  // handle merging a tracking device that is not enabled.  It should cancel existing stops created by that device
  @Test
  public void synchronizeFor_disabledDevice() throws Exception {
    final Location preprocessedLocation = position1.toLocation();
    final Location location = Location.builder(preprocessedLocation)
        .name("Foobar")
        .build();
    when(locator.reverseLookup(preprocessedLocation)).thenReturn(location, location);
    trackingDevice = TrackingDevice.builder(trackingDevice)
        .key(DEVICEDBID)
        .lastLocation(location)
        .lastActualLocation(null)
        .build();
    when(locationResolver.resolve(position1, trackingDevice, account)).thenReturn(location);
    when(trackingDeviceDAO.findAll()).thenReturn(ImmutableList.of(trackingDevice));
    when(connector.findPositions(account)).thenReturn(ImmutableList.of(position1));
    when(blacklistLocationMatcher.isBlacklisted(null)).thenReturn(false);
    TruckStop stop = TruckStop.builder()
        .createdWithDeviceId(DEVICEDBID)
        .startTime(now.minusHours(1))
        .endTime(now.plusHours(1))
        .key(789L)
        .location(location)
        .truck(mock(Truck.class))
        .build();
    when(truckStopCache.get(TRUCKID)).thenReturn(ImmutableList.of(stop));
    when(clock.now()).thenReturn(now);

    service.synchronizeFor(account);

    verify(trackingDeviceDAO).save(TrackingDevice.builder(trackingDevice)
        .lastActualLocation(position1.toLocation())
        .fuelLevel(position1.getFuelLevel())
        .batteryCharge(position1.getBatteryCharge())
        .build());
    verify(locator, times(2)).reverseLookup(preprocessedLocation);
    verify(truckStopDAO).save(TruckStop.builder(stop)
        .endTime(now)
        .appendNote("Ended stop since beacon was disabled")
        .build());
  }

  @Test
  public void synchronizeFor_disableForLunchTruckInMorning() throws Exception {
    position1 = Position.builder(position1)
        .speedMph(0)
        .build();
    final Location preprocessedLocation = position1.toLocation();
    final Location location = Location.builder(preprocessedLocation)
        .name("Foobar")
        .build();
    when(locator.reverseLookup(preprocessedLocation)).thenReturn(location, location);
    trackingDevice = TrackingDevice.builder(trackingDevice)
        .key(DEVICEDBID)
        .enabled(true)
        .lastLocation(location)
        .lastActualLocation(null)
        .build();
    when(locationResolver.resolve(position1, trackingDevice, account)).thenReturn(location);
    when(trackingDeviceDAO.findAll()).thenReturn(ImmutableList.of(trackingDevice));
    when(connector.findPositions(account)).thenReturn(ImmutableList.of(position1));
    when(blacklistLocationMatcher.isBlacklisted(null)).thenReturn(false);
    TruckStop stop = TruckStop.builder()
        .createdWithDeviceId(DEVICEDBID)
        .startTime(now.minusHours(1))
        .endTime(now.plusHours(1))
        .key(789L)
        .location(location)
        .truck(mock(Truck.class))
        .build();
    when(truckStopCache.get(TRUCKID)).thenReturn(ImmutableList.of(stop));
    when(clock.now()).thenReturn(now);
    when(clock.timeAt(11, 0)).thenReturn(now.withTime(11, 0, 0, 0));
    truck = Truck.builder(truck)
        .categories(ImmutableSet.of("Lunch"))
        .build();
    when(truckDAO.findById(TRUCKID)).thenReturn(truck);

    service.synchronizeFor(account);

    verify(trackingDeviceDAO).save(TrackingDevice.builder(trackingDevice)
        .lastActualLocation(position1.toLocation())
        .fuelLevel(position1.getFuelLevel())
        .batteryCharge(position1.getBatteryCharge())
        .parked(true)
        .build());
    verify(truckDAO).findById(TRUCKID);
    verify(locator, times(2)).reverseLookup(preprocessedLocation);
    verify(truckStopDAO).save(TruckStop.builder(stop)
        .endTime(now)
        .appendNote("Ended stop since beacon was disabled")
        .build());
  }



  // create a tracking device when none exists.  This tracking device is associated with the linxupaccount that
  // was queried.  During the merge phase it will try to cancel all stops, but none exist since nothing has been
  // created with this device yet.
  @Test
  public void synchronizeFor_bootstrapDevice() throws Exception {
    final Location preprocessedLocation = position1.toLocation();
    final Location location = Location.builder(preprocessedLocation)
        .name("Foobar")
        .build();
    when(locator.reverseLookup(preprocessedLocation)).thenReturn(location, location);
    when(trackingDeviceDAO.findAll()).thenReturn(ImmutableList.<TrackingDevice>of());
    when(connector.findPositions(account)).thenReturn(ImmutableList.of(position1));
    when(blacklistLocationMatcher.isBlacklisted(null)).thenReturn(false);
    when(locationResolver.resolve(position1, null, account)).thenReturn(preprocessedLocation);

    service.synchronizeFor(account);

    TrackingDevice trackingDevice = TrackingDevice.builder()
        .deviceNumber(position1.getDeviceNumber())
        .lastLocation(location)
        .lastActualLocation(position1.toLocation())
        .parked(false)
        .degreesFromNorth(position1.getDirection())
        .fuelLevel(position1.getFuelLevel())
        .batteryCharge(position1.getBatteryCharge())
        .atBlacklistedLocation(false)
        .truckOwnerId(TRUCKID)
        .lastBroadcast(position1.getDate())
        .label(position1.getVehicleLabel())
        .build();
    verify(trackingDeviceDAO).save(trackingDevice);
    verify(locator, times(2)).reverseLookup(preprocessedLocation);
  }
}