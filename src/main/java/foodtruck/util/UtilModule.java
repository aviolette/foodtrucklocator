package foodtruck.util;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author aviolette@gmail.com
 * @since 9/22/11
 */
public class UtilModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Clock.class).to(ClockImpl.class);
  }

  @Provides
  public DateTimeZone provideDefaultZone() {
    return DateTimeZone.forID("America/Chicago");
  }

  @TimeFormatter @Provides
  public DateTimeFormatter provideDateTimeFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("YYYYMMdd-HHmm").withZone(zone);
  }

  @TimeOnlyFormatter @Provides
  public DateTimeFormatter providesTimeOnlyFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("hh:mm a").withZone(zone);
  }

  @HtmlDateFormatter @Provides
  public DateTimeFormatter providesDateTimeFormatterForHtml(DateTimeZone zone) {
    return DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm").withZone(zone);
  }

  @FriendlyDateTimeFormat @Provides
  public DateTimeFormatter providesFriendlyDateTimeFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("MM/dd/YYYY hh:mm a").withZone(zone);
  }

  @FriendlyDateOnlyFormat @Provides
  public DateTimeFormatter providesFriendlyDateFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("MM/dd/YYYY").withZone(zone);
  }

  @DateOnlyFormatter @Provides
  public DateTimeFormatter providesDateOnlyFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("YYYYMMdd").withZone(zone);
  }

  @HttpHeaderFormat @Provides
  public DateTimeFormatter providesHttpHeaderFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("EEE, dd MMM YYYY hh:mm:ss a").withZone(zone);
  }

  @WeeklyRollup @Provides
  public Slots provideWeeklyRollup() {
    return new Slots(1000 * 60 * 60 * 24 * 7);
  }

  @DailyRollup @Provides
  public Slots provideDailyRollup() {
    return new Slots(1000 * 60 * 60 * 24);
  }

  @FifteenMinuteRollup @Provides
  public Slots provideFifteenMinuteRollup() {
    return new Slots(1000 * 60 * 15);
  }
}
