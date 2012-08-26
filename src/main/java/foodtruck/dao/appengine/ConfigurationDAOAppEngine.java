package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Throwables;
import com.google.inject.Inject;

import org.joda.time.DateTimeZone;

import foodtruck.dao.ConfigurationDAO;
import static foodtruck.dao.appengine.Attributes.getBooleanProperty;
import foodtruck.model.Configuration;

/**
 * @author aviolette@gmail.com
 * @since 4/11/12
 */
public class ConfigurationDAOAppEngine implements ConfigurationDAO {
  private final static String CONFIGURATION_KIND = "Configuration";
  private final DatastoreServiceProvider provider;
  private static final String PROP_GOOGLE_GEOLOCATION_ENABLED = "google_geolcation";
  private static final String PROP_YAHOO_GEOLOCATION_ENABLED = "yahoo_geolocation";
  private static final String PROP_GOOGLE_THROTTLE = "google_throttle_until";
  private static final String PROP_GOOGLE_TWEET_UPLOADING_ENABLED = "tweet_uploading";
  private final DateTimeZone defaultZone;

  @Inject
  public ConfigurationDAOAppEngine(DatastoreServiceProvider provider, DateTimeZone defaultZone) {
    this.provider = provider;
    this.defaultZone = defaultZone;
  }

  @Override
  public void save(Configuration config) {
    DatastoreService service = provider.get();
    service.put(toEntity(service, config));
  }

  private Entity toEntity(DatastoreService service, Configuration config) {
    Entity entity;
    if (config.isNew()) {
      entity = new Entity(CONFIGURATION_KIND);
    } else {
      try {
        entity = service.get((Key) config.getKey());
      } catch (EntityNotFoundException e) {
        throw Throwables.propagate(e);
      }
    }
    entity.setProperty(PROP_GOOGLE_GEOLOCATION_ENABLED, config.isGoogleGeolocationEnabled());
    entity.setProperty(PROP_YAHOO_GEOLOCATION_ENABLED, config.isYahooGeolocationEnabled());
    entity.setProperty(PROP_GOOGLE_TWEET_UPLOADING_ENABLED, config.isTweetUpdateServletEnabled());
    Attributes.setDateProperty(PROP_GOOGLE_THROTTLE, entity, config.getThrottleGoogleUntil());
    return entity;
  }

  @Override
  public Configuration findSingleton() {
    DatastoreService service = provider.get();
    Query q = new Query(CONFIGURATION_KIND);
    Entity entity = service.prepare(q).asSingleEntity();
    if (entity == null) {
      return Configuration.builder().build();
    }
    return fromEntity(entity);
  }

  private Configuration fromEntity(Entity entity) {
    return Configuration.builder()
        .googleGeolocationEnabled((Boolean) entity.getProperty(PROP_GOOGLE_GEOLOCATION_ENABLED))
        .yahooGeolocationEnabled((Boolean) entity.getProperty(PROP_YAHOO_GEOLOCATION_ENABLED))
        .tweetUpdateServletEnabled(getBooleanProperty(entity, PROP_GOOGLE_TWEET_UPLOADING_ENABLED))
        .throttleGoogleGeocoding(Attributes.getDateTime(entity, PROP_GOOGLE_THROTTLE, defaultZone))
        .key(entity.getKey())
        .build();
  }
}
