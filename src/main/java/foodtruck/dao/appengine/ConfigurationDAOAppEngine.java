package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;

import org.joda.time.DateTimeZone;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Configuration;

/**
 * @author aviolette@gmail.com
 * @since 4/11/12
 */
public class ConfigurationDAOAppEngine extends
    SingletonDAOAppEngine<Configuration> implements ConfigurationDAO {
  private final static String CONFIGURATION_KIND = "Configuration";
  private static final String PROP_GOOGLE_THROTTLE = "google_throttle_until";
  private final DateTimeZone defaultZone;

  @Inject
  public ConfigurationDAOAppEngine(DatastoreServiceProvider provider, DateTimeZone defaultZone) {
    super(provider, CONFIGURATION_KIND);
    this.defaultZone = defaultZone;
  }

  @Override protected Entity toEntity(Entity entity, Configuration config) {
    Attributes.setDateProperty(PROP_GOOGLE_THROTTLE, entity, config.getThrottleGoogleUntil());
    return entity;
  }

  protected Configuration fromEntity(Entity entity) {
    return Configuration.builder()
        .throttleGoogleGeocoding(Attributes.getDateTime(entity, PROP_GOOGLE_THROTTLE, defaultZone))
        .key(entity.getKey())
        .build();
  }

  @Override protected Configuration buildObject() {
    return Configuration.builder().build();
  }
}
