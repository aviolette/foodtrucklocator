package foodtruck.schedule;

import com.google.common.collect.ImmutableList;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.StopOrigin;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 3/29/16
 */
public abstract class AbstractSpecialMatcher implements SpecialMatcher {
  private final GeoLocator geoLocator;
  private final ImmutableList<Spot> commonSpots;
  private final DateTimeFormatter timeFormatter;
  private final Clock clock;

  protected AbstractSpecialMatcher(GeoLocator geoLocator, ImmutableList<Spot> commonSpots, DateTimeFormatter formatter,
      Clock clock) {
    this.geoLocator = geoLocator;
    this.commonSpots = commonSpots;
    this.timeFormatter = formatter;
    this.clock = clock;
  }

  public GeoLocator getGeoLocator() {
    return geoLocator;
  }

  public ImmutableList<Spot> getCommonSpots() {
    return commonSpots;
  }

  protected TruckStop.Builder truckStop(Story story, Truck truck) {
    String message = "Stop added from twitter story: '" + story.getFlattenedText() + "' at " + timeFormatter.print(clock.now());
    return TruckStop.builder()
        .truck(truck)
        .appendNote(message)
        .origin(StopOrigin.TWITTER)
        .locked(true);
  }
}
