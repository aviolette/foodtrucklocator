package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import org.joda.time.DateTimeZone;

import foodtruck.dao.FoodTruckRequestDAO;
import foodtruck.model.FoodTruckRequest;
import foodtruck.model.Location;
import static foodtruck.dao.appengine.Attributes.getDateTime;
import static foodtruck.dao.appengine.Attributes.getDoubleProperty;
import static foodtruck.dao.appengine.Attributes.getIntProperty;
import static foodtruck.dao.appengine.Attributes.getStringProperty;

/**
 * @author aviolette
 * @since 12/24/13
 */
public class FoodTruckRequestDAOAppEngine extends AppEngineDAO<Long, FoodTruckRequest>
    implements FoodTruckRequestDAO {
  private static final String KIND = "food_truck_request";
  private final DateTimeZone zone;

  @Inject
  public FoodTruckRequestDAOAppEngine(DatastoreServiceProvider provider, DateTimeZone zone) {
    super(KIND, provider);
    this.zone = zone;
  }

  @Override protected Entity toEntity(FoodTruckRequest obj, Entity entity) {
    entity.setProperty("description", obj.getDescription());
    entity.setProperty("email", obj.getEmail());
    entity.setProperty("userId", obj.getUserId());
    entity.setProperty("latitude", obj.getLocation().getLatitude());
    entity.setProperty("longitude", obj.getLocation().getLongitude());
    entity.setProperty("location_name", obj.getLocation().getName());
    entity.setProperty("phone", obj.getPhone());
    Attributes.setDateProperty("start_time", entity, obj.getStartTime());
    Attributes.setDateProperty("end_time", entity, obj.getStartTime());
    entity.setProperty("requester", obj.getRequester());
    entity.setProperty("expected_guests", obj.getExpectedGuests());
    entity.setProperty("prepaid", obj.isPrepaid());
    entity.setProperty("event_name", obj.getEventName());
    entity.setProperty("archived", obj.isArchived());
    Attributes.setDateProperty("submitted", entity, obj.getSubmitted());
    Attributes.setDateProperty("approved", entity, obj.getApproved());

    return entity;
  }

  @Override protected FoodTruckRequest fromEntity(Entity entity) {
    Location location = Location.builder()
        .lat(getDoubleProperty(entity, "latitude", 0d))
        .lng(getDoubleProperty(entity, "longitude", 0d))
        .name(getStringProperty(entity, "location_name"))
        .build();
    return FoodTruckRequest.builder()
        .key(entity.getKey().getId())
        .eventName(getStringProperty(entity, "event_name"))
        .requester(getStringProperty(entity, "requester"))
        .description(getStringProperty(entity, "description"))
        .email(getStringProperty(entity, "email"))
        .location(location)
        .submitted(getDateTime(entity, "submitted", zone))
        .approved(getDateTime(entity, "approved", zone))
        .archived(getBooleanProperty(entity, "archived", false))
        .userId(getStringProperty(entity, "userId"))
        .phone(getStringProperty(entity, "phone"))
        .startTime(getDateTime(entity, "start_time", zone))
        .endTime(getDateTime(entity, "end_time", zone))
        .expectedGuests(getIntProperty(entity, "expected_guests", 0))
        .prepaid(getBooleanProperty(entity, "prepaid", false))
        .build();
  }

  @Override public String getKind() {
    return KIND;
  }

  @Override public Iterable<FoodTruckRequest> findAllForUser(String userId) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(KIND);
    q.setFilter(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId));
    ImmutableSet.Builder<FoodTruckRequest> requests = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      requests.add(fromEntity(entity));
    }
    return requests.build();
  }
}
