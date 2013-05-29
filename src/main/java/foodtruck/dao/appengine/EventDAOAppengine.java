package foodtruck.dao.appengine;

import java.util.Collection;
import java.util.List;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import org.joda.time.DateTimeZone;

import foodtruck.dao.EventDAO;
import foodtruck.model.Event;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import static foodtruck.dao.appengine.Attributes.getDateTime;
import static foodtruck.dao.appengine.Attributes.getDoubleProperty;
import static foodtruck.dao.appengine.Attributes.getStringProperty;
import static foodtruck.dao.appengine.Attributes.setDateProperty;

/**
 * @author aviolette
 * @since 5/28/13
 */
public class EventDAOAppengine extends AppEngineDAO<String, Event> implements EventDAO {
  private static final String EVENT_KIND = "event";
  private static final String FIELD_NAME = "name";
  private static final String FIELD_START_TIME = "start_time";
  private static final String FIELD_END_TIME = "end_time";
  private static final String FIELD_LOCATION_NAME = "location_name";
  private static final String FIELD_LOCATION_LAT = "location_lat";
  private static final String FIELD_LOCATION_LNG = "location_lng";
  private static final String FIELD_URL = "url";
  private static final String FIELD_DESCRIPTION = "description";
  private static final String FIELD_TRUCKS = "trucks";
  private final DateTimeZone zone;

  @Inject
  public EventDAOAppengine(DatastoreServiceProvider provider, DateTimeZone zone) {
    super(EVENT_KIND, provider);
    this.zone = zone;
  }

  @Override protected void modifyFindAllQuery(Query q) {
    q.addSort(FIELD_START_TIME, Query.SortDirection.DESCENDING);
  }

  @Override protected Entity toEntity(Event event, Entity entity) {
    entity.setProperty(FIELD_NAME, event.getName());
    setDateProperty(FIELD_START_TIME, entity, event.getStartTime());
    setDateProperty(FIELD_END_TIME, entity, event.getEndTime());
    entity.setProperty(FIELD_LOCATION_NAME, event.getLocation().getName());
    entity.setProperty(FIELD_LOCATION_LAT, event.getLocation().getLatitude());
    entity.setProperty(FIELD_LOCATION_LNG, event.getLocation().getLongitude());
    entity.setProperty(FIELD_URL, event.getUrl());
    entity.setProperty(FIELD_DESCRIPTION, event.getDescription());
    entity.setProperty(FIELD_TRUCKS, toTruckList(event.getTrucks()));
    return entity;
  }

  @Override protected Event fromEntity(Entity entity) {
    Location location = Location.builder()
        .name(getStringProperty(entity, FIELD_LOCATION_NAME))
        .lat(getDoubleProperty(entity, FIELD_LOCATION_LAT, 0.0))
        .lng(getDoubleProperty(entity, FIELD_LOCATION_LNG, 0.0))
        .build();
    return Event.builder()
        .key(entity.getKey().getName())
        .location(location)
        .trucks(fromTruckList(entity.getProperty(FIELD_TRUCKS)))
        .name(getStringProperty(entity, FIELD_NAME))
        .startTime(getDateTime(entity, FIELD_START_TIME, zone))
        .endTime(getDateTime(entity, FIELD_END_TIME, zone))
        .description(getStringProperty(entity, FIELD_DESCRIPTION))
        .url(getStringProperty(entity, FIELD_URL))
        .build();
  }

  private List<Truck> fromTruckList(Object truckProperty) {
    return FluentIterable.from((Collection) truckProperty)
        .transform(new Function<EmbeddedEntity, Truck>() {
          @Override public Truck apply(EmbeddedEntity embeddedEntity) {
            return Truck.builder()
                .name((String) embeddedEntity.getProperty("name"))
                .id((String) embeddedEntity.getProperty("id"))
                .build();
          }
        })
        .toList();
  }

  private List<EmbeddedEntity> toTruckList(List<Truck> trucks) {
    return FluentIterable.from(trucks)
        .transform(new Function<Truck, EmbeddedEntity>() {
          @Override public EmbeddedEntity apply(Truck truck) {
            EmbeddedEntity truckEntity = new EmbeddedEntity();
            truckEntity.setProperty("name", truck.getName());
            truckEntity.setProperty("id", truck.getId());
            return truckEntity;
          }
        })
        .toList();
  }
}
