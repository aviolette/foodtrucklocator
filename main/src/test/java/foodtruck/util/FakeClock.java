package foodtruck.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import foodtruck.time.Clock;
import foodtruck.time.DayOfWeek;

/**
 * @author aviolette
 * @since 2018-12-20
 */
public class FakeClock implements Clock {

  private final long epochMillis;
  private final String zone;

  public static Clock fixed(long millis) {
    return new FakeClock(millis);
  }

  private FakeClock(long now) {
    this.epochMillis = now;
    this.zone = "America/Chicago";
  }

  @Override
  public DateTime now() {
    return new DateTime(epochMillis, zone());
  }

  @Override
  public ZonedDateTime now8() {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.of(zone));
  }

  @Override
  public LocalDate currentDay() {
    return now().toLocalDate();
  }

  @Override
  public DayOfWeek dayOfWeek() {
    throw new UnsupportedOperationException("dayOfWeek");
  }

  @Override
  public LocalDate firstDayOfWeekFrom(DateTime dt) {
    throw new UnsupportedOperationException("firstDayOfWeekFrom");
  }

  @Override
  public DateTimeZone zone() {
    return DateTimeZone.forID(zone);
  }

  @Override
  public ZoneId zone8() {
    return ZoneId.of(zone);
  }

  @Override
  public String nowFormattedAsTime() {
    return DateTimeFormatter.ISO_ZONED_DATE_TIME.format(now8());
  }

  @Override
  public DateTime timeAt(int hour, int minute) {
    throw new UnsupportedOperationException("timeAt");
  }

  @Override
  public long nowInMillis() {
    return epochMillis;
  }
}
