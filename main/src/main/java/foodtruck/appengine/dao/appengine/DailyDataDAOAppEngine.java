package foodtruck.appengine.dao.appengine;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import foodtruck.dao.DailyDataDAO;
import foodtruck.model.DailyData;

import static com.google.appengine.api.datastore.Query.CompositeFilterOperator.and;
import static com.google.appengine.api.datastore.Query.FilterOperator.EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.NOT_EQUAL;

/**
 * @author aviolette
 * @since 10/26/15
 */
class DailyDataDAOAppEngine extends AppEngineDAO<Long, DailyData> implements DailyDataDAO {
  private static final String SPECIALS_LOCATION_ID = "location_id";
  private static final String SPECIALS_SPECIALS = "specials";
  private static final String SPECIALS_DATE = "date";
  private static final String SPECIALS_KIND = "specials";
  private static final String SPECIALS_TRUCK_ID = "truck_id";
  private final DateTimeZone defaultZone;

  @Inject
  public DailyDataDAOAppEngine(Provider<DatastoreService> provider, DateTimeZone zone) {
    super(SPECIALS_KIND, provider);
    this.defaultZone = zone;
  }

  @Nullable
  @Override
  public DailyData findByLocationAndDay(String locationId, LocalDate date) {
    return aq().filter(and(predicate(SPECIALS_LOCATION_ID, EQUAL, locationId),
        predicate(SPECIALS_DATE, EQUAL, date.toDateTimeAtStartOfDay(defaultZone)
            .toDate())))
        .findOne();
  }

  @Override
  @Nullable
  public DailyData findByTruckAndDay(String truckId, LocalDate date) {
    return aq().filter(and(predicate(SPECIALS_TRUCK_ID, EQUAL, truckId),
        predicate(SPECIALS_DATE, EQUAL, date.toDateTimeAtStartOfDay(defaultZone)
            .toDate())))
        .findOne();
  }

  @Override
  public List<DailyData> findTruckSpecialsByDay(LocalDate day) {
    return aq().filter(and(predicate(SPECIALS_TRUCK_ID, NOT_EQUAL, null), predicate(SPECIALS_DATE, EQUAL,
        day.toDateTimeAtStartOfDay(defaultZone)
            .toDate())))
        .execute();
  }

  @Override
  protected Entity toEntity(DailyData dailyData, Entity entity) {
    entity.setProperty(SPECIALS_LOCATION_ID, dailyData.getLocationId());
    entity.setProperty(SPECIALS_TRUCK_ID, dailyData.getTruckId());
    List<String> entities = Lists.newArrayListWithCapacity(dailyData.getSpecials()
        .size());
    for (DailyData.SpecialInfo info : dailyData.getSpecials()) {
      // TODO: can we use embedded entities for this, because this is obviously horrible
      entities.add(info.getSpecial() + ":" + info.isSoldOut());
    }
    entity.setProperty(SPECIALS_SPECIALS, entities);
    Attributes.setDateProperty(SPECIALS_DATE, entity, dailyData.getOnDate()
        .toDateTimeAtStartOfDay(defaultZone));
    return entity;
  }

  @Override
  protected DailyData fromEntity(Entity entity) {
    DailyData.Builder builder = DailyData.builder()
        .key(entity.getKey()
            .getId())
        .locationId(Attributes.getStringProperty(entity, SPECIALS_LOCATION_ID))
        .truckId(Attributes.getStringProperty(entity, SPECIALS_TRUCK_ID))
        .onDate(Attributes.getDateTime(entity, SPECIALS_DATE, defaultZone)
            .toLocalDate());
    Collection<String> specialsList = (Collection) entity.getProperty(SPECIALS_SPECIALS);
    if (specialsList != null) {
      for (String specialEncoded : specialsList) {
        String[] pieces = specialEncoded.split(":");
        builder.addSpecial(pieces[0], Boolean.parseBoolean(pieces[1]));
      }
    }
    return builder.build();
  }
}
