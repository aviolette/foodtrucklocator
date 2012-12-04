package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;

import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.model.Location;
import foodtruck.model.TwitterNotificationAccount;
import static foodtruck.dao.appengine.Attributes.getDoubleProperty;
import static foodtruck.dao.appengine.Attributes.getStringProperty;

/**
 * @author aviolette
 * @since 12/3/12
 */
public class TwitterNotificationAccountDAOAppEngine extends AppEngineDAO<Long, TwitterNotificationAccount>
    implements TwitterNotificationAccountDAO {

  private static final String PROP_LOCATION_NAME = "location_name";
  private static final String PROP_LOCATION_LAT = "location_lat";
  private static final String PROP_LOCATION_LNG = "location_lng";
  private static final String PROP_ACCESS_TOKEN = "access_token";
  private static final String PROP_ACCESS_TOKEN_SECRET = "access_token_secret";

  @Inject
  public TwitterNotificationAccountDAOAppEngine(DatastoreServiceProvider provider) {
    super("twitter_notification_account", provider);
  }

  @Override protected Entity toEntity(TwitterNotificationAccount obj, Entity entity) {
    entity.setProperty(PROP_LOCATION_NAME, obj.getLocation().getName());
    entity.setProperty(PROP_LOCATION_LAT, obj.getLocation().getLatitude());
    entity.setProperty(PROP_LOCATION_LNG, obj.getLocation().getLongitude());
    entity.setProperty(PROP_ACCESS_TOKEN, obj.getOauthToken());
    entity.setProperty(PROP_ACCESS_TOKEN_SECRET, obj.getOauthTokenSecret());
    return entity;
  }

  @Override protected TwitterNotificationAccount fromEntity(Entity entity) {
    Location location = Location.builder()
        .name((String) entity.getProperty(PROP_LOCATION_NAME))
        .lat(getDoubleProperty(entity, PROP_LOCATION_LAT, 0.0d))
        .lng(getDoubleProperty(entity, PROP_LOCATION_LNG, 0.0d))
        .build();
    return TwitterNotificationAccount.builder()
        .location(location)
        .oauthToken(getStringProperty(entity, PROP_ACCESS_TOKEN))
        .oauthTokenSecret(getStringProperty(entity, PROP_ACCESS_TOKEN_SECRET))
        .build();
  }
}
