package foodtruck.dao;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;

import foodtruck.model.Specials;

/**
 * @author aviolette
 * @since 10/26/15
 */
public interface SpecialsDAO extends DAO<Long, Specials> {
  @Nullable Specials findByLocationAndDay(String locationName, LocalDate date);
}
