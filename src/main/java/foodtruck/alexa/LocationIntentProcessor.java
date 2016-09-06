package foodtruck.alexa;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.LocalDate;

import foodtruck.dao.LocationDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;
import foodtruck.schedule.ScheduleCacher;
import foodtruck.server.resources.json.DailyScheduleWriter;
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
  private final DailyScheduleWriter dailyScheduleWriter;

  @Inject
  public LocationIntentProcessor(GeoLocator locator, FoodTruckStopService service, Clock clock, LocationDAO locationDAO,
      ScheduleCacher cacher, DailyScheduleWriter dailyScheduleWriter) {
    this.locator = locator;
    this.service = service;
    this.clock = clock;
    this.locationDAO = locationDAO;
    scheduleCacher = cacher;
    this.dailyScheduleWriter = dailyScheduleWriter;
  }

  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    Slot locationSlot = intent.getSlot(SLOT_LOCATION);
    if (locationSlot.getValue() == null) {
      List<String> locations = findAlternateLocations(false, null, clock.currentDay());
      if (locations.isEmpty()) {
        locations = ImmutableList.of("Clark and Monroe");
      }
      return SpeechletResponseBuilder.builder()
          .speechSSML(String.format(
              "You can ask me about a specific location in Chicago.  For example, you can say: What food trucks are at %s today?",
              locations.get(0)))
          .useSpeechTextForReprompt()
          .tell();
    }
    Location location = locator.locate(locationSlot.getValue(), GeolocationGranularity.NARROW);
    boolean tomorrow = "tomorrow".equals(intent.getSlot(SLOT_WHEN)
        .getValue());
    LocalDate requestDate = tomorrow ? clock.currentDay()
        .plusDays(1) : clock.currentDay();
    if (location == null || !location.isResolved()) {
      List<String> locations = findAlternateLocations(tomorrow, null, requestDate);
      log.log(Level.SEVERE, "Could not find location {0} that is specified in alexa", locationSlot.getValue());
      String messageText = String.format(
          "I'm sorry but I don't recognize that location.  You can ask about popular food truck stops in Chicago, such as %s",
          AlexaUtils.toAlexaList(locations, true, Conjunction.or));
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
        String nearby = AlexaUtils.toAlexaList(findAlternateLocations(tomorrow, location, requestDate), true,
            Conjunction.or);
        String noTrucks;
        if (Strings.isNullOrEmpty(nearby)) {
          noTrucks = String.format(
              "There are no trucks %sat %s %s and there don't appear to be any nearby that location.", futurePhrase,
              locationSlot.getValue(), dateRepresentation);
          builder.speechSSML(noTrucks);
        } else {
          return builder.speechSSML(String.format(
              "There are no trucks %sat %s %s.  These nearby locations have food trucks: %s.  You can ask me what food trucks are those locations.",
              futurePhrase, locationSlot.getValue(), dateRepresentation, nearby))
              .useSpeechTextForReprompt()
              .ask();
        }
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
    return builder.simpleCard("Food Trucks at " + location.getShortenedName())
        .tell();
  }

  private List<String> findAlternateLocations(boolean tomorrow, Location currentLocation, LocalDate theDate) {
    try {
      String schedule = tomorrow ? scheduleCacher.findTomorrowsSchedule() : scheduleCacher.findSchedule();
      if (tomorrow && Strings.isNullOrEmpty(schedule)) {
        schedule = dailyScheduleWriter.asJSON(service.findStopsForDay(theDate))
            .toString();
        scheduleCacher.saveTomorrowsSchedule(schedule);
      }
      JSONObject jsonObject = new JSONObject(schedule);
      log.log(Level.FINE, "Schedule {0}", jsonObject);
      JSONArray locationArr = jsonObject.getJSONArray("locations");
      List<Location> locations = Lists.newLinkedList();
      for (int i = 0; i < locationArr.length(); i++) {
        Long key = locationArr.getJSONObject(i)
            .getLong("key");
        Location loc = locationDAO.findById(key);
        if (loc == null) {
          continue;
        }
        locations.add(loc);
      }

      Predicate<Location> filterPredicate = Predicates.alwaysTrue();
      if (currentLocation != null) {
        Collections.sort(locations, currentLocation.distanceFromComparator());
        filterPredicate = currentLocation.rangedPredicate(5);
      }
      return FluentIterable.from(locations)
          .filter(filterPredicate)
          .transform(Location.TO_SPOKEN_NAME)
          .limit(3)
          .toList();
    } catch (Exception e) {
      log.log(Level.WARNING, "Could not parse daily schedule {0} {1}", new Object[]{tomorrow, e.getMessage()});
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
