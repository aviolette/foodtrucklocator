package foodtruck.appengine.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.NotificationDeviceProfileDAO;
import foodtruck.model.NotificationDeviceProfile;

/**
 * @author aviolette
 * @since 2/14/16
 */
class NotificationDeviceProfileDAOAppEngine extends AppEngineDAO<String, NotificationDeviceProfile> implements NotificationDeviceProfileDAO {
  private static final String KIND = "notification_device_profile";
  private static final String LOCATION_NAMES = "location_names";
  private static final String TRUCK_IDS = "truck_ids";

  @Inject
  public NotificationDeviceProfileDAOAppEngine(Provider<DatastoreService> provider) {
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
        .deviceToken(entity.getKey()
            .getName())
        .locationNames(Attributes.getListProperty(entity, LOCATION_NAMES))
        .truckIds(Attributes.getListProperty(entity, TRUCK_IDS))
        .build();
  }
}
