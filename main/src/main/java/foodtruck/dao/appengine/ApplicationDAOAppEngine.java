package foodtruck.dao.appengine;

import java.util.Collection;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.ApplicationDAO;
import foodtruck.model.Application;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static foodtruck.dao.appengine.Attributes.getStringProperty;

/**
 * @author aviolette
 * @since 1/25/13
 */
class ApplicationDAOAppEngine extends AppEngineDAO<String, Application> implements ApplicationDAO {
  private static final String KIND = "application";
  private static final String PROP_NAME = "name";
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_ENABLED = "enabled";
  private static final String RATE_LIMIT = "rate_limit";
  private static final String CAN_HANDLE_NOTIFICATIONS = "can_handle_notifications";

  @Inject
  public ApplicationDAOAppEngine(Provider<DatastoreService> provider) {
    super(KIND, provider);
  }

  @Override
  protected Entity toEntity(Application obj, Entity entity) {
    entity.setProperty(PROP_NAME, obj.getName());
    entity.setProperty(PROP_DESCRIPTION, obj.getDescription());
    entity.setProperty(PROP_ENABLED, obj.isEnabled());
    entity.setProperty(RATE_LIMIT, obj.isRateLimitEnabled());
    entity.setProperty(CAN_HANDLE_NOTIFICATIONS, obj.canHandleNotifications());
    return entity;
  }

  @Override
  protected Application fromEntity(Entity entity) {
    return Application.builder()
        .name(getStringProperty(entity, PROP_NAME))
        .rateLimit(getBooleanProperty(entity, RATE_LIMIT, false))
        .description(getStringProperty(entity, PROP_DESCRIPTION))
        .enabled(getBooleanProperty(entity, PROP_ENABLED, false))
        .canHandleNotifications(getBooleanProperty(entity, CAN_HANDLE_NOTIFICATIONS, false))
        .appKey(entity.getKey()
            .getName())
        .build();
  }

  @Override
  public Collection<Application> findActive() {
    return aq().filter(predicate(PROP_ENABLED, EQUAL, true))
        .execute();
  }
}
