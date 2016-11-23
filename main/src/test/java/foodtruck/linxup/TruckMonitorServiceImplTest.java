package foodtruck.linxup;

import com.google.appengine.api.taskqueue.Queue;
import com.google.common.collect.ImmutableList;
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
import foodtruck.server.security.SecurityChecker;
import foodtruck.util.Clock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author aviolette
 * @since 11/22/16
 */
@RunWith(MockitoJUnitRunner.class)
public class TruckMonitorServiceImplTest {
  private static final String TRUCKID = "truckid";
  private static final String DEVICEID1 = "device1";
  private static final String DEVICELABEL1 = "label1";
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
  @Mock private TruckMonitorServiceImpl service;
  private LinxupAccount account;
  private DateTime now = new DateTime(2016, 11, 22, 9, 0, 0);

  @Before
  public void before() {
    account = LinxupAccount.builder()
        .enabled(true)
        .key(123L)
        .truckId(TRUCKID)
        .username("username")
        .password("password")
        .build();
    service = new TruckMonitorServiceImpl(truckStopDAO, connector, trackingDeviceDAO, locator, clock, truckDAO,
        formatter, securityChecker, linxupAccountDAO, queueProvider, Providers.of(truckStopCache),
        blacklistLocationMatcher);
  }

  // create a tracking device when none exists.  This tracking device is associated with the linxupaccount that
  // was queried.  During the merge phase it will try to cancel all stops, but none exist since nothing has been
  // created with this device yet.
  @Test
  public void synchronizeFor_bootstrapDevice() throws Exception {
    Position position = Position.builder()
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
    final Location preprocessedLocation = position.toLocation();
    final Location location = Location.builder(preprocessedLocation)
        .name("Foobar")
        .build();
    when(locator.reverseLookup(preprocessedLocation)).thenReturn(location, location);
    when(trackingDeviceDAO.findAll()).thenReturn(ImmutableList.<TrackingDevice>of());
    when(connector.findPositions(account)).thenReturn(ImmutableList.of(position));
    when(blacklistLocationMatcher.isBlacklisted(null)).thenReturn(false);
    service.synchronizeFor(account);

    TrackingDevice trackingDevice = TrackingDevice.builder()
        .deviceNumber(position.getDeviceNumber())
        .lastLocation(location)
        .lastActualLocation(position.toLocation())
        .parked(false)
        .degreesFromNorth(position.getDirection())
        .fuelLevel(position.getFuelLevel())
        .batteryCharge(position.getBatteryCharge())
        .atBlacklistedLocation(false)
        .truckOwnerId(TRUCKID)
        .lastBroadcast(position.getDate())
        .label(position.getVehicleLabel())
        .build();
    verify(trackingDeviceDAO).save(trackingDevice);
    verify(locator, times(2)).reverseLookup(preprocessedLocation);
  }
}