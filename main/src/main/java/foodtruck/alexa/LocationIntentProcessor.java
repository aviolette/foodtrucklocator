package foodtruck.alexa;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.LocationDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;
import foodtruck.schedule.ScheduleCacher;
import foodtruck.server.resources.json.DailyScheduleWriter;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;
import foodtruck.util.TimeOnlyFormatter;

import static foodtruck.alexa.AlexaModule.TESTING_MODE;
import static foodtruck.util.MoreStrings.capitalize;

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
  private final Location defaultCenter;
  private final DateTimeFormatter formatter;
  private final boolean testingMode;

  @Inject
  public LocationIntentProcessor(GeoLocator locator, FoodTruckStopService service, Clock clock, LocationDAO locationDAO,
      ScheduleCacher cacher, DailyScheduleWriter dailyScheduleWriter, @DefaultCenter Location center,
      @TimeOnlyFormatter DateTimeFormatter formatter, @Named(TESTING_MODE) boolean testingMode) {
    this.locator = locator;
    this.service = service;
    this.clock = clock;
    this.locationDAO = locationDAO;
    scheduleCacher = cacher;
    this.dailyScheduleWriter = dailyScheduleWriter;
    this.defaultCenter = center;
    this.formatter = formatter;
    this.testingMode = testingMode;
  }

  @Override
  public Set<String> getSlotNames() {
    return ImmutableSet.of(SLOT_LOCATION, SLOT_WHEN);
  }

  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    Slot locationSlot = intent.getSlot(SLOT_LOCATION);
    String locationName = locationSlot.getValue();
    if (Strings.isNullOrEmpty(locationName)) {
      return provideHelp();
    } else if (locationName.endsWith(" for")) {
      locationName = locationName.substring(0, locationName.length() - 4);
    }
    Location location = locator.locate(locationName, GeolocationGranularity.NARROW);
    boolean tomorrow = "tomorrow".equals(intent.getSlot(SLOT_WHEN)
        .getValue());
    LocalDate currentDay = clock.currentDay();
    LocalDate requestDate = tomorrow ? currentDay.plusDays(1) : currentDay;
    if (testingMode && (location == null || !location.isResolved())) {
      log.info("In testing mode.  Using Willis Tower for " + locationName);
      location = locator.locate("Willis Tower", GeolocationGranularity.NARROW);
    }
    if (location == null || !location.isResolved()) {
      return locationNotFound(locationName, tomorrow, requestDate);
    }
    boolean inFuture = requestDate.isAfter(currentDay);
    String dateRepresentation = toDate(requestDate);
    SpeechletResponseBuilder builder = SpeechletResponseBuilder.builder();
    @SuppressWarnings("unchecked") List<TruckStop> trucks = FluentIterable.from(
        service.findStopsNearALocation(location, requestDate))
        .toList();
    List<String> truckNames = Lists.transform(trucks, TruckStop.TO_TRUCK_NAME);
    List<String> truckTimes = Lists.transform(trucks, new Function<TruckStop, String>() {
      public String apply(TruckStop input) {
        return formatter.print(input.getStartTime()) + " to " + formatter.print(input.getEndTime());
      }
    });
    boolean allSame = Sets.newHashSet(truckTimes)
        .size() == 1;
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
              location.getShortenedName(), dateRepresentation);
          builder.speechSSML(noTrucks);
        } else {
          builder.speechSSML(
              String.format("There are no trucks %sat %s %s.  These nearby locations have food trucks: %s.",
                  futurePhrase, location.getShortenedName(), dateRepresentation, nearby));
        }
        break;
      case 1:
        builder.speechSSML(
            String.format("%s is %sat %s %s from %s", truckNames.get(0), futurePhrase, location.getShortenedName(),
                dateRepresentation, truckTimes.get(0)));
        break;
      case 2: {
        String schedulePart = allSame ? " from " + truckTimes.get(0) : buildSchedulePart(truckNames, truckTimes);
        builder.speechSSML(
            String.format("%s and %s are %sat %s %s%s", truckNames.get(0), truckNames.get(1), futurePhrase,
                location.getShortenedName(), dateRepresentation, schedulePart));
      }
        break;
      default: {
        String schedulePart = allSame ? " from " + truckTimes.get(0) : buildSchedulePart(truckNames, truckTimes);
        builder.speechSSML(
            String.format("There are %s trucks %sat %s %s: %s%s", count, futurePhrase, location.getShortenedName(),
                dateRepresentation, AlexaUtils.toAlexaList(truckNames, true), schedulePart));
      }
    }
    String cardTitle = String.format("Food Trucks at %s %s", location.getShortenedName(),
        capitalize(toDate(requestDate)));
    if (location.getImageUrl() == null) {
      builder.simpleCard(cardTitle);
    } else {
      builder.imageCard(cardTitle, location.getImageUrl()
          .secure(), location.getImageUrl()
          .secure());
    }
    return builder.tell();
  }

  private String buildSchedulePart(List<String> truckNames, List<String> truckTimes) {
    List<String> combined = Lists.newLinkedList();
    for (int i = 0; i < truckNames.size(); i++) {
      combined.add(truckNames.get(i) + " from " + truckTimes.get(i));
    }
    return ". " + AlexaUtils.toAlexaList(combined, true);
  }

  private SpeechletResponse provideHelp() {
    List<String> locations = findAlternateLocations(false, null, clock.currentDay());
    if (locations.isEmpty()) {
      locations = ImmutableList.of("Clark and Monroe");
    }
    return SpeechletResponseBuilder.builder()
        .speechSSML(String.format(
            "You can ask me about a specific location in Chicago.  For example, you can say: What food trucks are at %s today?",
            locations.get(0)))
        .useSpeechTextForReprompt()
        .ask();
  }

  private SpeechletResponse locationNotFound(String locationName, boolean tomorrow, LocalDate requestDate) {
    List<String> locations = findAlternateLocations(tomorrow, null, requestDate);
    log.log(Level.SEVERE, "Could not find location {0} that is specified in alexa", locationName);
    SpeechletResponseBuilder builder = SpeechletResponseBuilder.builder();
    // See "Don’t Blame the User" https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/alexa-skills-kit-voice-design-best-practices
    if (locations.isEmpty()) {
      return builder.speechSSML("What location was that?")
          .useSpeechTextForReprompt()
          .tell();
    } else {
      return builder.speechSSML("What location was that?")
          .repromptSSML(String.format(
              "I did not recognize the location you mentioned.  Some locations that have trucks %s are %s.  Which location are you interested in?",
              toDate(requestDate), AlexaUtils.toAlexaList(locations, true, Conjunction.or)))
          .ask();
    }
  }

  private List<String> findAlternateLocations(boolean tomorrow, Location currentLocation, LocalDate theDate) {
    try {
      String schedule = tomorrow ? scheduleCacher.findTomorrowsSchedule() : scheduleCacher.findSchedule();
      if (tomorrow && Strings.isNullOrEmpty(schedule)) {
        schedule = dailyScheduleWriter.asJSON(service.findStopsForDay(theDate))
            .toString();
        scheduleCacher.saveTomorrowsSchedule(schedule);
      }
      List<Location> locations = extractLocations(schedule);
      Predicate<Location> filterPredicate = Predicates.alwaysTrue();
      Location searchLocation = MoreObjects.firstNonNull(currentLocation, defaultCenter);
      if (searchLocation != null) {
        Collections.sort(locations, searchLocation.distanceFromComparator());
        if (currentLocation != null) {
          filterPredicate = currentLocation.rangedPredicate(5);
        }
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

  private List<Location> extractLocations(String schedule) throws JSONException {
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
    return locations;
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
