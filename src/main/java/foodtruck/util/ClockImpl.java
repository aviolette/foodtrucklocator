package foodtruck.util;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

/**
 * @author aviolette@gmail.com
 * @since 9/22/11
 */
public class ClockImpl implements Clock {
  private final DateTimeZone zone;

  @Inject
  public ClockImpl(DateTimeZone zone) {
    this.zone = zone;
  }

  @Override
  public DateTime now() {
    return new DateTime(zone);
  }

  @Override
  public LocalDate currentDay() {
    return new LocalDate(zone);
  }
}
