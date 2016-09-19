package foodtruck.dao.appengine;

import java.util.Map;

import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.joda.time.DateTimeZone;

import foodtruck.dao.AlexaExchangeDAO;
import foodtruck.model.AlexaExchange;

import static foodtruck.dao.appengine.Attributes.getDateTime;
import static foodtruck.dao.appengine.Attributes.setDateProperty;

/**
 * @author aviolette
 * @since 9/16/16
 */
class AlexaExchangeDAOAppEngine extends AppEngineDAO<Long, AlexaExchange> implements AlexaExchangeDAO {

  private final DateTimeZone zone;

  @Inject
  public AlexaExchangeDAOAppEngine(DatastoreServiceProvider provider, DateTimeZone zone) {
    super("alexa_exchange", provider);
    this.zone = zone;
  }

  @Override
  protected Entity toEntity(AlexaExchange obj, Entity entity) {
    entity.setProperty("intent", obj.getIntentName());
    setDateProperty("requested", entity, obj.getRequestTime());
    setDateProperty("completed", entity, obj.getCompleteTime());
    for (Map.Entry<String, String> slot : obj.getSlots()
        .entrySet()) {
      entity.setProperty("prop_" + slot.getKey(), slot.getValue());
    }
    return entity;
  }

  @Override
  protected AlexaExchange fromEntity(Entity entity) {
    AlexaExchange.Builder alexaBuilder = AlexaExchange.builder();
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    for (Map.Entry<String, Object> entry : entity.getProperties()
        .entrySet()) {
      if (entry.getKey()
          .startsWith("prop_")) {
        builder.put(entry.getKey()
            .substring(5), entry.getValue()
            .toString());
      } else if ("intent".equals(entry.getKey())) {
        alexaBuilder.intentName(entry.getValue()
            .toString());
      }
    }
    alexaBuilder.slots(builder.build());
    return alexaBuilder.requested(getDateTime(entity, "requested", zone))
        .completeTime(getDateTime(entity, "completed", zone))
        .build();
  }
}
