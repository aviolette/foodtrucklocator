package foodtruck.time;

import java.time.ZoneId;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import static org.joda.time.DateTimeZone.UTC;

/**
 * @author aviolette@gmail.com
 * @since 9/22/11
 */
public class TimeModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Clock.class).to(ClockImpl.class);
  }

  @Provides
  public DateTimeZone provideDefaultZone() {
    return DateTimeZone.forID(System.getProperty("foodtrucklocator.timezone", "America/Chicago"));
  }

  @Provides
  ZoneId providesZoneId() {
    return ZoneId.of(System.getProperty("foodtrucklocator.timezone", "America/Chicago"));
  }

  @TimeFormatter @Provides
  public DateTimeFormatter provideDateTimeFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("YYYYMMdd-HHmm").withZone(zone);
  }

  @TimeOnlyFormatter @Provides
  public DateTimeFormatter providesTimeOnlyFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("hh:mm a").withZone(zone);
  }

  @MilitaryTimeOnlyFormatter @Provides
  public DateTimeFormatter providesMilitaryTimeOnlyFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("HH:mm").withZone(zone);
  }

  @FacebookTimeFormat @Provides
  public DateTimeFormatter providesFacebookTimeFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZ").withZone(zone);
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

  @AlexaDateFormat
  @Provides
  public DateTimeFormatter providesAlexDateTimeFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("YYYY-MM-dd").withZone(UTC);
  }

  @HttpHeaderFormat @Provides
  public DateTimeFormatter providesHttpHeaderFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("EEE, dd-MMM-YYYY hh:mm:ss a").withZone(zone);
  }
}
