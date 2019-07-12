package foodtruck.appengine.dao.appengine;

import java.util.List;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import foodtruck.dao.MessageDAO;
import foodtruck.model.Message;

import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL;
import static com.google.appengine.api.datastore.Query.SortDirection.ASCENDING;

/**
 * @author aviolette
 * @since 2/6/14
 */
class MessageDAOAppEngine extends AppEngineDAO<Long, Message> implements MessageDAO {

  private static final String MESSAGE_KIND = "message";
  private final DateTimeZone zone;

  @Inject
  public MessageDAOAppEngine(Provider<DatastoreService> provider, DateTimeZone zone) {
    super(MESSAGE_KIND, provider);
    this.zone = zone;
  }

  @Override
  protected void modifyFindAllQuery(Query q) {
    q.addSort("startTime", ASCENDING);
  }

  @Override
  protected Entity toEntity(Message obj, Entity entity) {
    entity.setProperty("message", obj.getMessage());
    Attributes.setDateProperty("startTime", entity, obj.getStartTime());
    Attributes.setDateProperty("endTime", entity, obj.getEndTime());
    return entity;
  }

  @Override
  protected Message fromEntity(Entity entity) {
    return new Message(entity.getKey()
        .getId(), (String) entity.getProperty("message"), Attributes.getDateTime(entity, "startTime", zone),
        Attributes.getDateTime(entity, "endTime", zone));
  }

  @Override
  @Nullable
  public Message findByDay(LocalDate day) {
    DateTime instant = day.toDateTimeAtStartOfDay(zone), tomorrowExclusive = instant.plusDays(1)
        .minusMillis(1);
    // TODO: this code is wildly inefficient
    for (Message m : aq().filter(predicate("endTime", GREATER_THAN_OR_EQUAL, instant.toDate()))
        .sort("endTime", ASCENDING)
        .execute()) {
      boolean endsAfterTomorrow = m.getEndTime()
          .plusDays(1)
          .isAfter(tomorrowExclusive);
      boolean startsBeforeToday = m.getStartTime()
          .isBefore(instant.plusMinutes(1));
      if (endsAfterTomorrow && startsBeforeToday) {
        return m;
      }
    }
    return null;
  }

  @Override
  public List<Message> findExpiresAfter(LocalDate dateTime) {
    return aq().filter(predicate("endTime", GREATER_THAN_OR_EQUAL, dateTime.toDateTimeAtCurrentTime()
        .toDate()))
        .execute();
  }
}
