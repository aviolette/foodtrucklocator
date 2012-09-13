package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;

import org.joda.time.DateTimeZone;

import foodtruck.dao.ConfigurationDAO;
import static foodtruck.dao.appengine.Attributes.getBooleanProperty;
import foodtruck.model.Configuration;

/**
 * @author aviolette@gmail.com
 * @since 4/11/12
 */
public class ConfigurationDAOAppEngine extends
    AppEngineSingletonDAO<Configuration> implements ConfigurationDAO {
  private final static String CONFIGURATION_KIND = "Configuration";
  private static final String PROP_GOOGLE_GEOLOCATION_ENABLED = "google_geolcation";
  private static final String PROP_YAHOO_GEOLOCATION_ENABLED = "yahoo_geolocation";
  private static final String PROP_GOOGLE_THROTTLE = "google_throttle_until";
  private static final String PROP_GOOGLE_TWEET_UPLOADING_ENABLED = "tweet_uploading";
  private final DateTimeZone defaultZone;

  @Inject
  public ConfigurationDAOAppEngine(DatastoreServiceProvider provider, DateTimeZone defaultZone) {
    super(provider, CONFIGURATION_KIND);
    this.defaultZone = defaultZone;
  }

  @Override protected Entity toEntity(Entity entity, Configuration config) {
    entity.setProperty(PROP_GOOGLE_GEOLOCATION_ENABLED, config.isGoogleGeolocationEnabled());
    entity.setProperty(PROP_YAHOO_GEOLOCATION_ENABLED, config.isYahooGeolocationEnabled());
    entity.setProperty(PROP_GOOGLE_TWEET_UPLOADING_ENABLED, config.isTweetUpdateServletEnabled());
    Attributes.setDateProperty(PROP_GOOGLE_THROTTLE, entity, config.getThrottleGoogleUntil());
    return entity;
  }

  protected Configuration fromEntity(Entity entity) {
    return Configuration.builder()
        .googleGeolocationEnabled((Boolean) entity.getProperty(PROP_GOOGLE_GEOLOCATION_ENABLED))
        .yahooGeolocationEnabled((Boolean) entity.getProperty(PROP_YAHOO_GEOLOCATION_ENABLED))
        .tweetUpdateServletEnabled(getBooleanProperty(entity, PROP_GOOGLE_TWEET_UPLOADING_ENABLED))
        .throttleGoogleGeocoding(Attributes.getDateTime(entity, PROP_GOOGLE_THROTTLE, defaultZone))
        .key(entity.getKey())
        .build();
  }

  @Override protected Configuration buildObject() {
    return Configuration.builder().build();
  }
}
