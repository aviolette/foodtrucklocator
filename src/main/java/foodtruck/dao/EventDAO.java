package foodtruck.dao;

import java.util.List;

import org.joda.time.DateTime;

import foodtruck.model.Event;

/**
 * @author aviolette
 * @since 5/28/13
 */
public interface EventDAO extends DAO<String, Event> {
  /**
   * Finds all events that happen after the specified time, sorted by start time.
   */
  List<Event> findEventsAfter(DateTime time);
}
