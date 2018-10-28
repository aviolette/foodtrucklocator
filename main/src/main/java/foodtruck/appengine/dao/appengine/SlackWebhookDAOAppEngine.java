package foodtruck.appengine.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.SlackWebhookDAO;
import foodtruck.model.SlackWebhook;

import static foodtruck.appengine.dao.appengine.Attributes.getStringProperty;

/**
 * @author aviolette
 * @since 10/28/18
 */
public class SlackWebhookDAOAppEngine extends AppEngineDAO<Long, SlackWebhook> implements SlackWebhookDAO {

  @Inject
  public SlackWebhookDAOAppEngine(Provider<DatastoreService> provider) {
    super("slack_webhook", provider);
  }

  @Override
  protected void modifyFindAllQuery(Query q) {
    q.addSort("location_name", Query.SortDirection.ASCENDING);
  }

  @Override
  protected Entity toEntity(SlackWebhook obj, Entity entity) {
    entity.setProperty("url", obj.getWebookUrl());
    entity.setProperty("location_name", obj.getLocationName());
    return entity;
  }

  @Override
  protected SlackWebhook fromEntity(Entity entity) {
    return SlackWebhook.builder()
        .key(entity.getKey().getId())
        .webhookUrl(getStringProperty(entity, "url"))
        .locationName(getStringProperty(entity, "location_name"))
        .build();
  }
}
