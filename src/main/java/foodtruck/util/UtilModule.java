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

  @FriendlyDateOnlyFormat @Provides
  public DateTimeFormatter providesFriendlyDateFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("MM/dd/YYYY");
  }

  @DateOnlyFormatter @Provides
  public DateTimeFormatter providesDateOnlyFormat(DateTimeZone zone) {
    return DateTimeFormat.forPattern("YYYYMMdd");
  }
}
