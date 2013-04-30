package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;

import org.joda.time.DateTimeZone;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Configuration;
import foodtruck.model.Location;
import static foodtruck.dao.appengine.Attributes.getBooleanProperty;
import static foodtruck.dao.appengine.Attributes.getListProperty;
import static foodtruck.dao.appengine.Attributes.getStringProperty;

/**
 * @author aviolette@gmail.com
 * @since 4/11/12
 */
public class ConfigurationDAOAppEngine extends
    SingletonDAOAppEngine<Configuration> implements ConfigurationDAO {
  private final static String CONFIGURATION_KIND = "Configuration";
  private static final String PROP_GOOGLE_GEOLOCATION_ENABLED = "google_geolcation";
  private static final String PROP_YAHOO_GEOLOCATION_ENABLED = "yahoo_geolocation";
  private static final String PROP_GOOGLE_THROTTLE = "google_throttle_until";
  private static final String PROP_GOOGLE_TWEET_UPLOADING_ENABLED = "tweet_uploading";
  private final DateTimeZone defaultZone;
  private static final String PROP_CENTER_LATITUDE = "center_latitude";
  private static final String PROP_CENTER_LONGITUDE = "center_longitude";
  private static final String PROP_CENTER_NAME = "center_name";
  private static final String PROP_LOCAL_CACHE_ENABLED = "local_twitter_cache_enabled";
  private static final String PROP_REMOTE_CACHE_ENABLED = "remote_twitter_cache_enabled";
  private static final String PROP_REMOTE_CACHE_ADDRESS = "remote_twitter_cache_address";
  private static final String PROP_PRIMARY_TWITTER_LIST = "primary_twitter_list";
  private static final String PROP_GOOGLE_CALENDAR_ADDRESS = "google_calendar_address";
  private static final String PROP_YAHOO_APP_ID = "yahoo_app_id";
  private static final String PROP_YAHOO_CONSUMER_KEY = "yahoo_consumer_key";
  private static final String PROP_YAHOO_CONSUMER_SECRET = "yahoo_consumer_secret";
  private static final String PROP_SYSTEM_NOTIFICATION_EMAILS = "system_notification_receivers";
  private static final String PROP_SYSTEM_NOTIFICATION_SENDER = "system_notification_sender";

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
    entity.setProperty(PROP_CENTER_NAME, config.getCenter().getName());
    entity.setProperty(PROP_CENTER_LATITUDE, config.getCenter().getLatitude());
    entity.setProperty(PROP_CENTER_LONGITUDE, config.getCenter().getLongitude());
    entity.setProperty(PROP_LOCAL_CACHE_ENABLED, config.isLocalTwitterCachingEnabled());
    entity.setProperty(PROP_REMOTE_CACHE_ENABLED, config.isRemoteTwitterCachingEnabled());
    entity.setProperty(PROP_REMOTE_CACHE_ADDRESS, config.getRemoteTwitterCacheAddress());
    entity.setProperty(PROP_GOOGLE_CALENDAR_ADDRESS, config.getGoogleCalendarAddress());
    entity.setProperty(PROP_PRIMARY_TWITTER_LIST, config.getPrimaryTwitterList());
    entity.setProperty(PROP_YAHOO_CONSUMER_KEY, config.getYahooConsumerKey());
    entity.setProperty(PROP_YAHOO_CONSUMER_SECRET, config.getYahooConsumerSecret());
    entity.setProperty(PROP_YAHOO_APP_ID, config.getYahooAppId());
    entity.setProperty(PROP_SYSTEM_NOTIFICATION_SENDER, config.getNotificationSender());
    entity.setProperty(PROP_SYSTEM_NOTIFICATION_EMAILS, config.getSystemNotificationList());
    return entity;
  }

  protected Configuration fromEntity(Entity entity) {
    Location center =
        Location.builder()
            .lat(Attributes.getDoubleProperty(entity, PROP_CENTER_LATITUDE, 41.8807438))
            .lng(Attributes.getDoubleProperty(entity, PROP_CENTER_LONGITUDE, -87.6293867))
            .name((String) entity.getProperty(PROP_CENTER_NAME))
            .build();
    return Configuration.builder()
        .googleGeolocationEnabled((Boolean) entity.getProperty(PROP_GOOGLE_GEOLOCATION_ENABLED))
        .yahooGeolocationEnabled((Boolean) entity.getProperty(PROP_YAHOO_GEOLOCATION_ENABLED))
        .tweetUpdateServletEnabled(getBooleanProperty(entity, PROP_GOOGLE_TWEET_UPLOADING_ENABLED))
        .throttleGoogleGeocoding(Attributes.getDateTime(entity, PROP_GOOGLE_THROTTLE, defaultZone))
        .localTwitterCachingEnabled(getBooleanProperty(entity, PROP_LOCAL_CACHE_ENABLED, true))
        .remoteTwitterCachingEnabled(getBooleanProperty(entity, PROP_REMOTE_CACHE_ENABLED, false))
        .remoteTwitterCacheAddress((String) entity.getProperty(PROP_REMOTE_CACHE_ADDRESS))
        .googleCalendarAddress((String) entity.getProperty(PROP_GOOGLE_CALENDAR_ADDRESS))
        .primaryTwitterList((String) entity.getProperty(PROP_PRIMARY_TWITTER_LIST))
        .yahooAppId((String) entity.getProperty(PROP_YAHOO_APP_ID))
        .yahooConsumerKey(getStringProperty(entity, PROP_YAHOO_CONSUMER_KEY))
        .yahooConsumerSecret(getStringProperty(entity, PROP_YAHOO_CONSUMER_SECRET))
        .systemNotificationList(getListProperty(entity, PROP_SYSTEM_NOTIFICATION_EMAILS))
        .notificationSender(getStringProperty(entity, PROP_SYSTEM_NOTIFICATION_SENDER))
        .center(center)
        .key(entity.getKey())
        .build();
  }

  @Override protected Configuration buildObject() {
    return Configuration.builder().build();
  }
}
