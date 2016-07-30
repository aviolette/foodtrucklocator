package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;

import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.model.Location;
import foodtruck.model.TrackingDevice;
import foodtruck.util.Clock;

import static foodtruck.dao.appengine.Attributes.getDateTime;
import static foodtruck.dao.appengine.Attributes.getDoubleProperty;
import static foodtruck.dao.appengine.Attributes.getStringProperty;
import static foodtruck.dao.appengine.Attributes.setDateProperty;

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
  private final Clock clock;

  @Inject
  public TrackingDeviceDAOAppEngine(Clock clock, DatastoreServiceProvider provider) {
    super("tracking_device", provider);
    this.clock = clock;
  }

  @Override
  protected Entity toEntity(TrackingDevice obj, Entity entity) {
    entity.setProperty(LABEL, obj.getLabel());
    entity.setProperty(DEVICE_NUMBER, obj.getDeviceNumber());
    entity.setProperty(ENABLED, obj.isEnabled());
    entity.setProperty(TRUCK_OWNER_ID, obj.getTruckOwnerId());
    setDateProperty(LAST_MODIFIED, entity, clock.now());
    setDateProperty(LAST_BROADCAST, entity, obj.getLastBroadcast());
    if (obj.getLastLocation() != null) {
      entity.setProperty(LAST_LOCATION_NAME, obj.getLastLocation().getName());
      entity.setProperty(LAST_LOCATION_LAT, obj.getLastLocation().getLatitude());
      entity.setProperty(LAST_LOCATION_LNG, obj.getLastLocation().getLongitude());
    }
    return entity;
  }

  @Override
  protected TrackingDevice fromEntity(Entity entity) {
    double lat = getDoubleProperty(entity, LAST_LOCATION_LAT, 0),
        lng = getDoubleProperty(entity, LAST_LOCATION_LNG, 0);
    String name = getStringProperty(entity, LAST_LOCATION_NAME);
    TrackingDevice.Builder builder =  TrackingDevice.builder();
    if (lat != 0 || lng != 0) {
      builder.lastLocation(Location.builder().name(name).lat(lat).lng(lng).build());
    }
    return builder
        .truckOwnerId(getStringProperty(entity, TRUCK_OWNER_ID))
        .deviceNumber(getStringProperty(entity, DEVICE_NUMBER))
        .label(getStringProperty(entity, LABEL))
        .lastBroadcast(getDateTime(entity, LAST_BROADCAST, clock.zone()))
        .enabled(getBooleanProperty(entity, ENABLED, false))
        .key(entity.getKey().getId())
        .build();
  }
}
