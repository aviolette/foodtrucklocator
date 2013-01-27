package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;

import foodtruck.dao.ApplicationDAO;
import foodtruck.model.Application;
import static foodtruck.dao.appengine.Attributes.getStringProperty;

/**
 * @author aviolette
 * @since 1/25/13
 */
public class ApplicationDAOAppEngine extends AppEngineDAO<String, Application> implements ApplicationDAO {
  private static final String KIND = "application";
  private static final String PROP_NAME = "name";
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_ENABLED = "enabled";
  @Inject
  public ApplicationDAOAppEngine(DatastoreServiceProvider provider) {
    super(KIND, provider);
  }

  @Override protected Entity toEntity(Application obj, Entity entity) {
    entity.setProperty(PROP_NAME, obj.getName());
    entity.setProperty(PROP_DESCRIPTION, obj.getDescription());
    entity.setProperty(PROP_ENABLED, obj.isEnabled());
    return entity;
  }

  @Override protected Application fromEntity(Entity entity) {
    return Application.builder()
        .name(getStringProperty(entity, PROP_NAME))
        .description(getStringProperty(entity, PROP_DESCRIPTION))
        .enabled(getBooleanProperty(entity, PROP_ENABLED, false))
        .appKey(entity.getKey().getName())
        .build();
  }
}
