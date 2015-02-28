package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import org.joda.time.DateTimeZone;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Configuration;
import foodtruck.model.Location;
import foodtruck.schedule.Confidence;

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
  private final DateTimeZone defaultZone;
  private static final String PROP_CENTER_LATITUDE = "center_latitude";
  private static final String PROP_CENTER_LONGITUDE = "center_longitude";
  private static final String PROP_CENTER_NAME = "center_name";
  private static final String PROP_GOOGLE_CALENDAR_ADDRESS = "google_calendar_address";
  private static final String PROP_YAHOO_APP_ID = "yahoo_app_id";
  private static final String PROP_SYSTEM_NOTIFICATION_EMAILS = "system_notification_receivers";
  private static final String PROP_SYSTEM_NOTIFICATION_SENDER = "system_notification_sender";
  private static final String PROP_FRONT_DOOR_APP_KEY = "front_door_app_key";
  private static final String MINIMUM_CONFIDENCE_FOR_DISPLAY = "minimum_display_confidence";
  private static final String SYNC_URL = "sync_url";
  private static final String SYNC_APPKEY = "sync_appkey";

  @Inject
  public ConfigurationDAOAppEngine(DatastoreServiceProvider provider, DateTimeZone defaultZone) {
    super(provider, CONFIGURATION_KIND);
    this.defaultZone = defaultZone;
  }

  @Override protected Entity toEntity(Entity entity, Configuration config) {
    entity.setProperty(PROP_GOOGLE_GEOLOCATION_ENABLED, config.isGoogleGeolocationEnabled());
    entity.setProperty(PROP_YAHOO_GEOLOCATION_ENABLED, config.isYahooGeolocationEnabled());
    Attributes.setDateProperty(PROP_GOOGLE_THROTTLE, entity, config.getThrottleGoogleUntil());
    entity.setProperty(PROP_CENTER_NAME, config.getCenter().getName());
    entity.setProperty(PROP_CENTER_LATITUDE, config.getCenter().getLatitude());
    entity.setProperty(PROP_CENTER_LONGITUDE, config.getCenter().getLongitude());
    entity.setProperty(PROP_GOOGLE_CALENDAR_ADDRESS, config.getGoogleCalendarAddress());
    entity.setProperty(PROP_YAHOO_APP_ID, config.getYahooAppId());
    entity.setProperty(PROP_SYSTEM_NOTIFICATION_SENDER, config.getNotificationSender());
    entity.setProperty(PROP_SYSTEM_NOTIFICATION_EMAILS, config.getSystemNotificationList());
    entity.setProperty(PROP_FRONT_DOOR_APP_KEY, config.getFrontDoorAppKey());
    entity.setProperty(MINIMUM_CONFIDENCE_FOR_DISPLAY, config.getMinimumConfidenceForDisplay().toString());
    entity.setProperty(SYNC_URL, config.getSyncUrl());
    entity.setProperty(SYNC_APPKEY, config.getSyncAppKey());
    return entity;
  }

  protected Configuration fromEntity(Entity entity) {
    Location center =
        Location.builder()
            .lat(Attributes.getDoubleProperty(entity, PROP_CENTER_LATITUDE, 41.8807438))
            .lng(Attributes.getDoubleProperty(entity, PROP_CENTER_LONGITUDE, -87.6293867))
            .name((String) entity.getProperty(PROP_CENTER_NAME))
            .build();
    String minimumDisplayConfidence = getStringProperty(entity, MINIMUM_CONFIDENCE_FOR_DISPLAY);
    Confidence confidence = Strings.isNullOrEmpty(minimumDisplayConfidence) ? Confidence.HIGH : Confidence.valueOf(minimumDisplayConfidence);
    return Configuration.builder()
        .syncUrl(getStringProperty(entity, SYNC_URL))
        .syncAppKey(getStringProperty(entity, SYNC_APPKEY))
        .minimumConfidenceForDisplay(confidence)
        .googleGeolocationEnabled((Boolean) entity.getProperty(PROP_GOOGLE_GEOLOCATION_ENABLED))
        .yahooGeolocationEnabled((Boolean) entity.getProperty(PROP_YAHOO_GEOLOCATION_ENABLED))
        .throttleGoogleGeocoding(Attributes.getDateTime(entity, PROP_GOOGLE_THROTTLE, defaultZone))
        .googleCalendarAddress((String) entity.getProperty(PROP_GOOGLE_CALENDAR_ADDRESS))
        .yahooAppId((String) entity.getProperty(PROP_YAHOO_APP_ID))
        .systemNotificationList(getListProperty(entity, PROP_SYSTEM_NOTIFICATION_EMAILS))
        .notificationSender(getStringProperty(entity, PROP_SYSTEM_NOTIFICATION_SENDER))
        .frontDoorAppKey(getStringProperty(entity, PROP_FRONT_DOOR_APP_KEY))
        .center(center)
        .key(entity.getKey())
        .build();
  }

  @Override protected Configuration buildObject() {
    return Configuration.builder().build();
  }
}
