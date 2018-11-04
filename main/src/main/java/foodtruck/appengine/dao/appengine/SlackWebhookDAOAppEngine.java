package foodtruck.appengine.dao.appengine;

import java.util.Optional;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.SlackWebhookDAO;
import foodtruck.model.SlackWebhook;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
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
    entity.setProperty("access_token", obj.getAccessToken());
    entity.setProperty("team_id", obj.getTeamId());
    return entity;
  }

  @Override
  protected SlackWebhook fromEntity(Entity entity) {
    return SlackWebhook.builder()
        .key(entity.getKey().getId())
        .webhookUrl(getStringProperty(entity, "url"))
        .locationName(getStringProperty(entity, "location_name"))
        .teamId(getStringProperty(entity, "team_id"))
        .accessToken(getStringProperty(entity, "access_token"))
        .build();
  }

  @Override
  public Optional<SlackWebhook> findByTeamId(String teamId) {
    return Optional.ofNullable(aq().filter(predicate("team_id", EQUAL, teamId))
        .findOne());
  }
}
