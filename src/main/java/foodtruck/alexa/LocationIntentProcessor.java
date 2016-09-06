package foodtruck.alexa;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.LocalDate;

import foodtruck.dao.LocationDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;
import foodtruck.schedule.ScheduleCacher;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 8/25/16
 */
class LocationIntentProcessor implements IntentProcessor {
  static final String SLOT_LOCATION = "Location";
  static final String SLOT_WHEN = "When";
  private static final Logger log = Logger.getLogger(LocationIntentProcessor.class.getName());
  private final GeoLocator locator;
  private final FoodTruckStopService service;
  private final Clock clock;
  private final ScheduleCacher scheduleCacher;
  private final LocationDAO locationDAO;

  @Inject
  public LocationIntentProcessor(GeoLocator locator, FoodTruckStopService service, Clock clock, LocationDAO locationDAO,
      ScheduleCacher cacher) {
    this.locator = locator;
    this.service = service;
    this.clock = clock;
    this.locationDAO = locationDAO;
    scheduleCacher = cacher;
  }

  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    Slot locationSlot = intent.getSlot(SLOT_LOCATION);
    // IF null, display help
    Location location = locator.locate(locationSlot.getValue(), GeolocationGranularity.NARROW);
    boolean tomorrow = "tomorrow".equals(intent.getSlot(SLOT_WHEN)
        .getValue());
    LocalDate requestDate = tomorrow ? clock.currentDay()
        .plusDays(1) : clock.currentDay();
    if (location == null || !location.isResolved()) {
      List<String> locations = findAlternateLocations(tomorrow, null);
      log.log(Level.SEVERE, "Could not find location {0} that is specified in alexa", locationSlot.getValue());
      String messageText = String.format(
          "I'm sorry but I don't recognize that location.  You can ask about popular " + "food truck stops in Chicago, such as %s",
          AlexaUtils.toAlexaList(locations, true));
      return SpeechletResponseBuilder.builder()
          .speechSSML(messageText)
          .useSpeechTextForReprompt()
          .ask();
    }
    boolean inFuture = requestDate.isAfter(clock.currentDay());
    String dateRepresentation = toDate(requestDate);
    SpeechletResponseBuilder builder = SpeechletResponseBuilder.builder();
    @SuppressWarnings("unchecked") List<String> truckNames = FluentIterable.from(
        service.findStopsNearALocation(location, requestDate))
        .transform(TruckStop.TO_TRUCK_NAME)
        .toList();
    String futurePhrase = inFuture ? "scheduled to be " : "";
    int count = truckNames.size();
    switch (count) {
      case 0:
        String noTrucks = "There are no trucks at " + locationSlot.getValue() + " " + dateRepresentation +
            ".  Perhaps try " + AlexaUtils.toAlexaList(findAlternateLocations(tomorrow, location), true);
        builder.speechSSML(noTrucks);
        break;
      case 1:
        builder.speechSSML(String.format("%s is the only food truck %sat %s %s", truckNames.get(0), futurePhrase,
            locationSlot.getValue(), dateRepresentation));
        break;
      case 2:
        builder.speechSSML(String.format("%s and %s are %sat %s %s", truckNames.get(0), truckNames.get(1), futurePhrase,
            locationSlot.getValue(), dateRepresentation));
        break;
      default:
        builder.speechSSML(
            String.format("There are %s trucks %sat %s %s: %s", count, futurePhrase, locationSlot.getValue(),
                dateRepresentation, AlexaUtils.toAlexaList(truckNames, true)));
    }
    return builder.simpleCard("Food Trucks at " + locationSlot.getValue())
        .tell();
  }

  private List<String> findAlternateLocations(boolean tomorrow, Location currentLocation) {
    String schedule = tomorrow ? scheduleCacher.findTomorrowsSchedule() : scheduleCacher.findSchedule();
    try {
      JSONObject jsonObject = new JSONObject(schedule);
      log.log(Level.INFO, "Schedule {0}", jsonObject.toString(2));
      JSONArray locationArr = jsonObject.getJSONArray("locations");
      ImmutableList.Builder<String> builder = ImmutableList.builder();
      for (int i = 0; i < locationArr.length(); i++) {
        Long key = locationArr.getJSONObject(i)
            .getLong("key");
        Location loc = locationDAO.findById(key);
        if (loc != null) {
          builder.add(loc.getShortenedName());
        }
      }
      return builder.build();
    } catch (JSONException e) {
      return ImmutableList.of("Clark and Monroe");
    }
  }

  private String toDate(LocalDate date) {
    if (date.equals(clock.currentDay())) {
      return "today";
    } else if (date.minusDays(1)
        .equals(clock.currentDay())) {
      return "tomorrow";
    } else {
      return "on that date";
    }
  }
}
