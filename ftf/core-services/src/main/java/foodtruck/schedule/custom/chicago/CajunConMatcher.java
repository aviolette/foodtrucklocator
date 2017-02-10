package foodtruck.schedule.custom.chicago;

import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.schedule.AbstractSpecialMatcher;
import foodtruck.schedule.Spot;
import foodtruck.schedule.TruckStopMatch;
import foodtruck.time.Clock;
import foodtruck.time.FriendlyDateTimeFormat;

/**
 * @author aviolette
 * @since 5/11/16
 */
public class CajunConMatcher extends AbstractSpecialMatcher {

  private static String COURT_BUILDING = "1100 South Hamilton, Chicago, IL";

  private static Map<String, String> MAPPINGS = ImmutableMap.of("11th hamilton", COURT_BUILDING, "1100 s Hamilton",
      COURT_BUILDING, "1900 w jackson", "1900 West Jackson, Chicago, IL", "800 s paulina",
      "800 South Paulina, Chicago, IL");

  @Inject
  public CajunConMatcher(GeoLocator geoLocator, ImmutableList<Spot> commonSpots,
      @FriendlyDateTimeFormat DateTimeFormatter formatter, Clock clock) {
    super(geoLocator, commonSpots, formatter, clock);
  }

  @Override
  public void handle(TruckStopMatch.Builder builder, Story story, Truck truck) {
    String text = story.getText();
    if (text.contains("27th California") && story.getTime()
        .getHourOfDay() < 9) {
      text = text.replaceAll(" s\\. ", " s ");
      text = text.replaceAll(" w\\. ", " w ");
      text = text.replaceAll(" e\\. ", " e ");
      text = text.replaceAll(" n\\. ", " n ");
      for (Map.Entry<String, String> entry : MAPPINGS.entrySet()) {
        if (text.toLowerCase()
            .contains(entry.getKey())) {
          builder.appendStop(
              truckStop(story, truck).location(getGeoLocator().locate(entry.getValue(), GeolocationGranularity.NARROW))
                  .startTime(story.getTime()
                      .withTime(10, 30, 0, 0))
                  .endTime(story.getTime()
                      .withTime(13, 30, 0, 0))
                  .build());
          break;
        }
      }
    }
  }
}