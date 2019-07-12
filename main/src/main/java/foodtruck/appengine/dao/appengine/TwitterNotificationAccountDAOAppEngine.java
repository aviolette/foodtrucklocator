package foodtruck.appengine.dao.appengine;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.model.Location;
import foodtruck.model.TwitterNotificationAccount;

import static foodtruck.appengine.dao.appengine.Attributes.getDoubleProperty;
import static foodtruck.appengine.dao.appengine.Attributes.getStringProperty;

/**
 * @author aviolette
 * @since 12/3/12
 */
class TwitterNotificationAccountDAOAppEngine extends AppEngineDAO<Long, TwitterNotificationAccount> implements TwitterNotificationAccountDAO {

  private static final String PROP_LOCATION_NAME = "location_name";
  private static final String PROP_LOCATION_LAT = "location_lat";
  private static final String PROP_LOCATION_LNG = "location_lng";
  private static final String PROP_LOCATION_RADIUS = "location_radius";
  private static final String PROP_ACCESS_TOKEN = "access_token";
  private static final String PROP_ACCESS_TOKEN_SECRET = "access_token_secret";
  private static final String PROP_TWITTER_HANDLE = "twitter_handle";
  private static final String PROP_NAME = "name";
  private static final String PROP_ACTIVE = "active";

  @Inject
  public TwitterNotificationAccountDAOAppEngine(Provider<DatastoreService> provider) {
    super("twitter_notification_account", provider);
  }

  @Override
  protected Entity toEntity(TwitterNotificationAccount obj, Entity entity) {
    entity.setProperty(PROP_LOCATION_NAME, obj.getLocation()
        .getName());
    entity.setProperty(PROP_LOCATION_LAT, obj.getLocation()
        .getLatitude());
    entity.setProperty(PROP_LOCATION_LNG, obj.getLocation()
        .getLongitude());
    entity.setProperty(PROP_LOCATION_RADIUS, obj.getLocation()
        .getRadius());
    entity.setProperty(PROP_ACCESS_TOKEN, obj.getOauthToken());
    entity.setProperty(PROP_ACCESS_TOKEN_SECRET, obj.getOauthTokenSecret());
    entity.setProperty(PROP_TWITTER_HANDLE, obj.getTwitterHandle());
    entity.setProperty(PROP_ACTIVE, obj.isActive());
    entity.setProperty(PROP_NAME, obj.getName());
    return entity;
  }

  @Override
  protected void modifyFindAllQuery(Query q) {
    q.addSort(PROP_NAME, Query.SortDirection.ASCENDING);
  }

  @Override
  protected TwitterNotificationAccount fromEntity(Entity entity) {
    Location location = Location.builder()
        .name((String) entity.getProperty(PROP_LOCATION_NAME))
        .lat(getDoubleProperty(entity, PROP_LOCATION_LAT, 0.0d))
        .lng(getDoubleProperty(entity, PROP_LOCATION_LNG, 0.0d))
        .radius(getDoubleProperty(entity, PROP_LOCATION_RADIUS, 0.0d))
        .build();
    return TwitterNotificationAccount.builder()
        .location(location)
        .key(entity.getKey()
            .getId())
        .active(getBooleanProperty(entity, PROP_ACTIVE, true))
        .oauthToken(getStringProperty(entity, PROP_ACCESS_TOKEN))
        .name(getStringProperty(entity, PROP_NAME))
        .twitterHandle(getStringProperty(entity, PROP_TWITTER_HANDLE))
        .oauthTokenSecret(getStringProperty(entity, PROP_ACCESS_TOKEN_SECRET))
        .build();
  }

  @Override
  @Nullable
  public TwitterNotificationAccount findByLocationName(String name) {
    return findSingleItemByAttribute(PROP_LOCATION_NAME, name);
  }

  @Nullable
  @Override
  public TwitterNotificationAccount findByTwitterHandle(String twitterHandle) {
    return findSingleItemByAttribute(PROP_TWITTER_HANDLE, twitterHandle);
  }
}
