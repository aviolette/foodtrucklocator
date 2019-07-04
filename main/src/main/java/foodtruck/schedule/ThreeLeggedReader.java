package foodtruck.schedule;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.TempTruckStop;

public class ThreeLeggedReader implements StopReader {

  private static final Logger log = Logger.getLogger(ThreeLeggedReader.class.getName());
  public static final String THREELEGGEDTACO = "threeleggedtaco";

  private final ZoneId zone;
  private final CalendarAddressExtractor extractor;
  private final TruckDAO truckDAO;

  @Inject
  public ThreeLeggedReader(CalendarAddressExtractor extractor, TruckDAO truckDAO, ZoneId zone) {
    this.zone = zone;
    this.extractor = extractor;
    this.truckDAO = truckDAO;
  }

  @Override
  public List<TempTruckStop> findStops(String document) {
    try {
      JSONArray calendarItems = new JSONArray(document);
      ImmutableList.Builder<TempTruckStop> stops = ImmutableList.builder();
      truckDAO.findByIdOpt(THREELEGGEDTACO).ifPresent(truck -> {
        for (int i=0; i < calendarItems.length(); i++) {
          try {
            JSONObject obj = calendarItems.getJSONObject(i);
            Optional<Location> locationOpt = this.extractor.parse(obj.getString("title"), truck);
            if (locationOpt.isPresent()) {
              TempTruckStop stop = TempTruckStop.builder()
                  .calendarName(getCalendar())
                  .locationName(locationOpt.get()
                      .getName())
                  .startTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(obj.getLong("startDate")), zone))
                  .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(obj.getLong("endDate")), zone))
                  .truckId(THREELEGGEDTACO)
                  .build();
              stops.add(stop);
            } else {
              log.log(Level.WARNING, "Could not resolve location {0}", obj.getString("title"));
            }
          } catch (JSONException je) {
            log.log(Level.WARNING, je.getMessage(), je);
          }
        }
      });

      return stops.build();
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getCalendar() {
    return THREELEGGEDTACO;
  }
}
