package foodtruck.linxup;

import java.time.LocalTime;
import java.time.ZonedDateTime;

import com.google.inject.Inject;

import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2018-12-03
 */
public class ServiceWindowDetectorImpl implements ServiceWindowDetector {

  private Clock clock;

  @Inject
  public ServiceWindowDetectorImpl(Clock clock) {
    this.clock = clock;
  }

  @Override
  public boolean during() {
    ZonedDateTime now = clock.now8();
    LocalTime time = now.toLocalTime();
    return time.isAfter(LocalTime.of(3, 40)) && time.isBefore(LocalTime.of(4, 15));
  }
}
