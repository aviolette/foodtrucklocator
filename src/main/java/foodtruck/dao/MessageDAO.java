package foodtruck.dao;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;

import foodtruck.model.Message;

/**
 * @author aviolette
 * @since 2/6/14
 */
public interface MessageDAO extends DAO<Long, Message> {
  /**
   * Find the message that is valid on the specific day.
   */
  @Nullable Message findByDay(LocalDate day);
}
