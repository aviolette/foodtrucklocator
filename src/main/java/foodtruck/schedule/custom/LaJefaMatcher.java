package foodtruck.schedule.custom;

import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.StopOrigin;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.AbstractSpecialMatcher;
import foodtruck.schedule.Spot;
import foodtruck.schedule.TruckStopMatch;

/**
 * @author aviolette
 * @since 3/29/16
 */
public class LaJefaMatcher extends AbstractSpecialMatcher {
  private static final Logger log = Logger.getLogger(LaJefaMatcher.class.getName());
  private final TruckDAO truckDAO;

  @Inject
  public LaJefaMatcher(TruckDAO truckDAO, ImmutableList<Spot> spots, GeoLocator geoLocator) {
    super(geoLocator, spots);
    this.truckDAO = truckDAO;
  }

  @Override
  public void handle(TruckStopMatch.Builder builder, Story story, Truck truck) {
    if(!"patronachicago".equals(truck.getId())) {
      return;
    }
    String lowerTweet = story.getText().toLowerCase();
    String stripped = lowerTweet.replace(" ", "");

    int index = stripped.indexOf("lajefa");
    if (index == -1) {
      return;
    }
    stripped = stripped.substring(index);

    Truck laJefa = truckDAO.findById("lajefa");
    if (laJefa == null) {
      log.warning("La Jefa food truck not found.");
      return;
    }

    TruckStop primary = builder.getPrimaryStop();
    if (primary == null) {
      return;
    }

    for (Spot spot : getCommonSpots()) {
      if (spot.contains(stripped)) {
        builder.appendStop(TruckStop.builder()
            .startTime(primary.getStartTime())
            .endTime(primary.getEndTime())
            .origin(StopOrigin.TWITTER)
            .truck(laJefa)
            .locked(true)
            .location(getGeoLocator().locate(spot.getCanonicalForm(), GeolocationGranularity.NARROW))
            .build());
        return;
      }
    }
  }
}
