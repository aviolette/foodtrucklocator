package foodtruck.appengine.dao.appengine;

import java.time.ZoneId;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.joda.time.Interval;

import foodtruck.dao.TempTruckStopDAO;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;

import static com.google.appengine.api.datastore.Query.CompositeFilterOperator.and;
import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL;
import static com.google.appengine.api.datastore.Query.SortDirection.ASCENDING;
import static foodtruck.appengine.dao.appengine.Attributes.getStringProperty;
import static foodtruck.appengine.dao.appengine.Attributes.getZonedDateProperty;

/**
 * @author aviolette
 * @since 2018-12-10
 */
public class TempTruckStopDAOAppEngine extends AppEngineDAO<Long, TempTruckStop> implements TempTruckStopDAO {

  private ZoneId zone;

  @Inject
  public TempTruckStopDAOAppEngine(Provider<DatastoreService> provider, ZoneId zoneId) {
    super("temp_truck_stop", provider);
    this.zone = zoneId;
  }

  @Override
  protected Entity toEntity(TempTruckStop obj, Entity entity) {
    entity.setProperty("calendar_name", obj.getCalendarName());
    Attributes.setZonedDateProperty("start_time", entity, obj.getStartTime());
    Attributes.setZonedDateProperty("end_time", entity, obj.getEndTime());
    entity.setProperty("truck_id", obj.getTruckId());
    entity.setProperty("location_name", obj.getLocationName());
    return entity;
  }

  @Override
  protected TempTruckStop fromEntity(Entity entity) {
    return TempTruckStop.builder()
        .key(entity.getKey().getId())
        .calendarName(getStringProperty(entity, "calendar_name"))
        .endTime(getZonedDateProperty("end_time", entity, zone))
        .startTime(getZonedDateProperty("start_time", entity, zone))
        .locationName(getStringProperty(entity, "location_name"))
        .truckId(getStringProperty(entity, "truck_id"))
        .build();
  }

  @Override
  public List<TempTruckStop> findDuring(Interval range, @Nullable Truck searchTruck) {
    String truckId = searchTruck == null ? null : searchTruck.getId();
    return aq().filter(and(trucksOverRange(truckId,
        range)))
        .sort("start_time", ASCENDING)
        .execute();
  }

  private Collection<Query.Filter> trucksOverRange(String truckId, Interval range) {
    List<Query.Filter> filters = Lists.newLinkedList();
    filters.add(new Query.FilterPredicate("start_time", GREATER_THAN_OR_EQUAL, range.getStart()
        .toDate()));
    filters.add(new Query.FilterPredicate("start_time", Query.FilterOperator.LESS_THAN, range.getEnd()
        .toDate()));
    if (truckId != null) {
      filters.add(new Query.FilterPredicate("truck_id", EQUAL, truckId));
    }
    return filters;

  }

  @Override
  public void deleteAll() {
    deleteFromQuery(provider.get(), query());
  }
}
