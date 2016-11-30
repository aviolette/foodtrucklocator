package foodtruck.appengine.dao.appengine;

import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.joda.time.DateTimeZone;

import foodtruck.dao.AlexaExchangeDAO;
import foodtruck.model.AlexaExchange;

import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;

/**
 * @author aviolette
 * @since 9/16/16
 */
class AlexaExchangeDAOAppEngine extends AppEngineDAO<Long, AlexaExchange> implements AlexaExchangeDAO {
  private static final String INTENT = "intent";
  private static final String REQUESTED = "requested";
  private static final String COMPLETED = "completed";
  private static final String HAD_CARD = "had_card";
  private static final String HAD_REPROMPT = "had_reprompt";
  private final DateTimeZone zone;

  @Inject
  public AlexaExchangeDAOAppEngine(Provider<DatastoreService> provider, DateTimeZone zone) {
    super("alexa_exchange", provider);
    this.zone = zone;
  }

  @Override
  protected Entity toEntity(AlexaExchange obj, Entity entity) {
    entity.setProperty(INTENT, obj.getIntentName());
    Attributes.setDateProperty(REQUESTED, entity, obj.getRequestTime());
    Attributes.setDateProperty(COMPLETED, entity, obj.getCompleteTime());
    entity.setProperty(HAD_CARD, obj.getHadCard());
    entity.setProperty(HAD_REPROMPT, obj.getHadReprompt());
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
    return alexaBuilder.requested(Attributes.getDateTime(entity, REQUESTED, zone))
        .hadCard(getBooleanProperty(entity, HAD_CARD, false))
        .hadReprompt(getBooleanProperty(entity, HAD_REPROMPT, false))
        .completeTime(Attributes.getDateTime(entity, COMPLETED, zone))
        .build();
  }

  @Override
  public List<AlexaExchange> findMostRecentOfIntent(String intentName) {
    return aq().filter(predicate(INTENT, EQUAL, intentName))
        .sort(REQUESTED, Query.SortDirection.DESCENDING)
        .limit(50)
        .execute();
  }
}
