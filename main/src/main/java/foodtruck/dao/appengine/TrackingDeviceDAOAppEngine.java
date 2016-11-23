package foodtruck.dao.appengine;

import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.model.Location;
import foodtruck.model.TrackingDevice;
import foodtruck.util.Clock;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;

/**
 * @author aviolette
 * @since 7/28/16
 */
class TrackingDeviceDAOAppEngine extends AppEngineDAO<Long, TrackingDevice> implements TrackingDeviceDAO {
  private static final String LABEL = "label";
  private static final String DEVICE_NUMBER = "device_number";
  private static final String ENABLED = "enabled";
  private static final String TRUCK_OWNER_ID = "truck_owner_id";
  private static final String LAST_MODIFIED = "last_modified";
  private static final String LAST_BROADCAST = "last_broadcast";
  private static final String LAST_LOCATION_NAME = "last_location_name";
  private static final String LAST_LOCATION_LAT = "last_location_lat";
  private static final String LAST_LOCATION_LNG = "last_location_lng";
  private static final String BLACKLISTED = "blacklisted";
  private static final String PARKED = "parked";
  private static final String FUEL_LEVEL = "fuel_level";
  private static final String BATTERY_CHARGE = "battery_charge";
  private static final String DIRECTION = "direction";
  private static final String LAST_ACTUAL_LOCATION_LAT = "last_actual_lat";
  private static final String LAST_ACTUAL_LOCATION_LNG = "last_actual_lng";
  private final Clock clock;

  @Inject
  public TrackingDeviceDAOAppEngine(Clock clock, Provider<DatastoreService> provider) {
    super("tracking_device", provider);
    this.clock = clock;
  }

  @Override
  protected Entity toEntity(TrackingDevice obj, Entity entity) {
    FluidEntity fe = new FluidEntity(entity).prop(LABEL, obj.getLabel())
        .prop(DEVICE_NUMBER, obj.getDeviceNumber())
        .prop(TRUCK_OWNER_ID, obj.getTruckOwnerId())
        .prop(LAST_MODIFIED, clock.now())
        .prop(LAST_BROADCAST, obj.getLastBroadcast())
        .prop(PARKED, obj.isParked())
        .prop(FUEL_LEVEL, obj.getFuelLevel())
        .prop(BATTERY_CHARGE, obj.getBatteryCharge())
        .prop(BLACKLISTED, obj.isAtBlacklistedLocation())
        .prop(DIRECTION, obj.getDegreesFromNorth())
        .prop(ENABLED, obj.isEnabled());
    if (obj.getLastLocation() != null) {
      fe.prop(LAST_LOCATION_NAME, obj.getLastLocation()
          .getName())
          .prop(LAST_LOCATION_LAT, obj.getLastLocation()
              .getLatitude())
          .prop(LAST_LOCATION_LNG, obj.getLastLocation()
              .getLongitude());
    } else {
      fe.prop(LAST_LOCATION_NAME, null)
          .prop(LAST_LOCATION_LAT, 0)
          .prop(LAST_LOCATION_LNG, 0);
    }
    if (obj.getLastActualLocation() != null) {
      fe.prop(LAST_ACTUAL_LOCATION_LAT, obj.getLastActualLocation()
          .getLatitude());
      fe.prop(LAST_ACTUAL_LOCATION_LNG, obj.getLastActualLocation()
          .getLongitude());
    }
    return fe.toEntity();
  }

  @Override
  protected TrackingDevice fromEntity(Entity entity) {
    FluidEntity fe = new FluidEntity(entity);
    double lat = fe.doubleVal(LAST_LOCATION_LAT),
        lng = fe.doubleVal(LAST_LOCATION_LNG), actualLat = fe.doubleVal(LAST_ACTUAL_LOCATION_LAT),
        actualLng = fe.doubleVal(LAST_ACTUAL_LOCATION_LNG);
    String name = fe.stringVal(LAST_LOCATION_NAME);
    TrackingDevice.Builder builder = TrackingDevice.builder();
    if (lat != 0 && lng != 0) {
      builder.lastLocation(Location.builder()
          .name(name)
          .lat(lat)
          .lng(lng)
          .build());
    }
    if (actualLat != 0 && actualLng != 0) {
      builder.lastActualLocation(Location.builder()
          .lat(actualLat)
          .lng(actualLng)
          .build());
    }
    return builder.parked(fe.booleanVal(PARKED))
        .truckOwnerId(fe.stringVal(TRUCK_OWNER_ID))
        .deviceNumber(fe.stringVal(DEVICE_NUMBER))
        .label(fe.stringVal(LABEL))
        .batteryCharge(fe.stringVal(BATTERY_CHARGE))
        .fuelLevel(fe.stringVal(FUEL_LEVEL))
        .degreesFromNorth(fe.intValue(DIRECTION))
        .atBlacklistedLocation(fe.booleanVal(BLACKLISTED))
        .lastModified(fe.dateVal(LAST_MODIFIED, clock.zone()))
        .lastBroadcast(fe.dateVal(LAST_BROADCAST, clock.zone()))
        .enabled(fe.booleanVal(ENABLED))
        .key(fe.longId())
        .build();
  }

  @Override
  public List<TrackingDevice> findByTruckId(String truckId) {
    return aq().filter(predicate(TRUCK_OWNER_ID, EQUAL, truckId))
        .execute();
  }
}
