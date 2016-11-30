package foodtruck.util;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * @author aviolette@gmail.com
 * @since 9/22/11
 */
public class UtilModule extends AbstractModule {
  @Override
  protected void configure() {
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
