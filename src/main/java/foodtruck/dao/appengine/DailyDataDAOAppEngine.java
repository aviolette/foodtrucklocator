package foodtruck.dao.appengine;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import foodtruck.dao.DailyDataDAO;
import foodtruck.model.DailyData;

import static foodtruck.dao.appengine.Attributes.getDateTime;
import static foodtruck.dao.appengine.Attributes.getStringProperty;
import static foodtruck.dao.appengine.Attributes.setDateProperty;

/**
 * @author aviolette
 * @since 10/26/15
 */
public class DailyDataDAOAppEngine extends AppEngineDAO<Long, DailyData> implements DailyDataDAO {

  private static final String SPECIALS_LOCATION_ID = "location_id";
  private static final String SPECIALS_SPECIALS = "specials";
  private static final String SPECIALS_DATE = "date";
  private static final String SPECIALS_KIND = "specials";
  private final DateTimeZone defaultZone;

  @Inject
  public DailyDataDAOAppEngine(DatastoreServiceProvider provider, DateTimeZone zone) {
    super(SPECIALS_KIND, provider);
    this.defaultZone = zone;
  }

  @Override
  protected Entity toEntity(DailyData dailyData, Entity entity) {
    entity.setProperty(SPECIALS_LOCATION_ID, dailyData.getLocationId());

    List<String> entities = Lists.newArrayListWithCapacity(dailyData.getSpecials().size());
    for (DailyData.SpecialInfo info : dailyData.getSpecials()) {
      // TODO: can we use embedded entities for this, because this is obviously horrible
      entities.add(info.getSpecial() + ":" + info.isSoldOut());
    }
    entity.setProperty(SPECIALS_SPECIALS, entities);
    setDateProperty(SPECIALS_DATE, entity, dailyData.getOnDate().toDateTimeAtStartOfDay());
    return entity;
  }

  @Override
  protected DailyData fromEntity(Entity entity) {
    DailyData.Builder builder = DailyData.builder()
        .key(entity.getKey().getId())
        .locationId(getStringProperty(entity, SPECIALS_LOCATION_ID))
        .onDate(getDateTime(entity, SPECIALS_DATE, defaultZone).toLocalDate());
    Collection<String> specialsList = (Collection) entity.getProperty(SPECIALS_SPECIALS);
    for (String specialEncoded : specialsList) {
      String[] pieces = specialEncoded.split(":");
      builder.addSpecial(pieces[0], Boolean.parseBoolean(pieces[1]));
    }
    return builder.build();
  }

  @Nullable
  @Override
  public DailyData findByLocationAndDay(String locationId, LocalDate date) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(SPECIALS_KIND);
    List<Query.Filter> filters = ImmutableList.<Query.Filter>of(
        new Query.FilterPredicate(SPECIALS_LOCATION_ID, Query.FilterOperator.EQUAL, locationId),
        new Query.FilterPredicate(SPECIALS_DATE, Query.FilterOperator.EQUAL, date.toDateTimeAtStartOfDay(defaultZone).toDate()));
    q.setFilter(Query.CompositeFilterOperator.and(filters));
    Entity entity = dataStore.prepare(q).asSingleEntity();
    if (entity == null) {
      return null;
    }
    return fromEntity(entity);
  }
}
