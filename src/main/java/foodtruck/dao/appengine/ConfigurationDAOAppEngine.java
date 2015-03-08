package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import org.joda.time.DateTimeZone;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Configuration;
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
  private static final String PROP_GOOGLE_THROTTLE = "google_throttle_until";
  private final DateTimeZone defaultZone;
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
    Attributes.setDateProperty(PROP_GOOGLE_THROTTLE, entity, config.getThrottleGoogleUntil());
    entity.setProperty(PROP_SYSTEM_NOTIFICATION_SENDER, config.getNotificationSender());
    entity.setProperty(PROP_SYSTEM_NOTIFICATION_EMAILS, config.getSystemNotificationList());
    entity.setProperty(PROP_FRONT_DOOR_APP_KEY, config.getFrontDoorAppKey());
    entity.setProperty(MINIMUM_CONFIDENCE_FOR_DISPLAY, config.getMinimumConfidenceForDisplay().toString());
    entity.setProperty(SYNC_URL, config.getSyncUrl());
    entity.setProperty(SYNC_APPKEY, config.getSyncAppKey());
    return entity;
  }

  protected Configuration fromEntity(Entity entity) {
    String minimumDisplayConfidence = getStringProperty(entity, MINIMUM_CONFIDENCE_FOR_DISPLAY);
    Confidence confidence = Strings.isNullOrEmpty(minimumDisplayConfidence) ? Confidence.HIGH : Confidence.valueOf(minimumDisplayConfidence);
    return Configuration.builder()
        .syncUrl(getStringProperty(entity, SYNC_URL))
        .syncAppKey(getStringProperty(entity, SYNC_APPKEY))
        .minimumConfidenceForDisplay(confidence)
        .throttleGoogleGeocoding(Attributes.getDateTime(entity, PROP_GOOGLE_THROTTLE, defaultZone))
        .systemNotificationList(getListProperty(entity, PROP_SYSTEM_NOTIFICATION_EMAILS))
        .notificationSender(getStringProperty(entity, PROP_SYSTEM_NOTIFICATION_SENDER))
        .frontDoorAppKey(getStringProperty(entity, PROP_FRONT_DOOR_APP_KEY))
        .key(entity.getKey())
        .build();
  }

  @Override protected Configuration buildObject() {
    return Configuration.builder().build();
  }
}
