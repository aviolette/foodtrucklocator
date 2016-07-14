package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;

import foodtruck.dao.NotificationDeviceProfileDAO;
import foodtruck.model.NotificationDeviceProfile;

import static foodtruck.dao.appengine.Attributes.getListProperty;

/**
 * @author aviolette
 * @since 2/14/16
 */
class NotificationDeviceProfileDAOAppEngine extends AppEngineDAO<String, NotificationDeviceProfile> implements NotificationDeviceProfileDAO {
  private static final String KIND = "notification_device_profile";
  private static final String LOCATION_NAMES = "location_names";
  private static final String TRUCK_IDS = "truck_ids";

  @Inject
  public NotificationDeviceProfileDAOAppEngine(DatastoreServiceProvider provider) {
    super(KIND, provider);
  }

  @Override
  protected Entity toEntity(NotificationDeviceProfile obj, Entity entity) {
    entity.setProperty(LOCATION_NAMES, obj.getLocationNames());
    entity.setProperty(TRUCK_IDS, obj.getTruckIds());
    return entity;
  }

  @Override
  protected NotificationDeviceProfile fromEntity(Entity entity) {
    return NotificationDeviceProfile.builder()
        .deviceToken(entity.getKey().getName())
        .locationNames(getListProperty(entity, LOCATION_NAMES))
        .truckIds(getListProperty(entity, TRUCK_IDS))
        .build();
  }
}
