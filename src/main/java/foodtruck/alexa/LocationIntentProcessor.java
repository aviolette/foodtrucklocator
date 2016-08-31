package foodtruck.alexa;

import java.util.List;
import java.util.logging.Logger;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import org.joda.time.LocalDate;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.TruckStop;
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

  @Inject
  public LocationIntentProcessor(GeoLocator locator, FoodTruckStopService service, Clock clock) {
    this.locator = locator;
    this.service = service;
    this.clock = clock;
  }

  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    Slot locationSlot = intent.getSlot(SLOT_LOCATION);
    Location location = locator.locate(locationSlot.getValue(), GeolocationGranularity.NARROW);
    if (location == null) {
      return SpeechletResponseBuilder.builder()
          .speechText(
              "I'm sorry but I don't recognize that location.  You can ask about popular food truck stops in Chicago, such as Clark and Monroe.")
          .useSpeechTextForReprompt()
          .ask();
    }
    // TODO: what if location cannot be found?
    LocalDate requestDate = "tomorrow".equals(intent.getSlot(SLOT_WHEN).getValue()) ? clock.currentDay()
        .plusDays(1) : clock.currentDay();
    boolean inFuture = requestDate.isAfter(clock.currentDay());
    String dateRepresentation = toDate(requestDate);
    String noTrucks = "There are no trucks at " + locationSlot.getValue() + " " + dateRepresentation;
    SpeechletResponseBuilder builder = SpeechletResponseBuilder.builder();
    if (location == null) {
      log.severe("Could not find location " + locationSlot.getValue() + " that is specified in alexa");
      return builder.speechText(noTrucks).simpleCard(locationSlot.getValue()).tell();
    } else {
      @SuppressWarnings("unchecked") List<String> truckNames = FluentIterable.from(
          service.findStopsNearALocation(location, requestDate)).transform(TruckStop.TO_TRUCK_NAME).toList();
      String futurePhrase = inFuture ? "scheduled to be " : "";
      int count = truckNames.size();
      switch (count) {
        case 0:
          builder.speechText(noTrucks);
          break;
        case 1:
          builder.speechSSML(String.format("%s is the only food truck %sat %s %s", truckNames.get(0), futurePhrase,
              locationSlot.getValue(), dateRepresentation));
          break;
        case 2:
          builder.speechSSML(
              String.format("%s and %s are %sat %s %s", truckNames.get(0), truckNames.get(1), futurePhrase,
                  locationSlot.getValue(), dateRepresentation));
          break;
        default:
          builder.speechSSML(
              String.format("There are %s trucks %sat %s %s: %s", count, futurePhrase, locationSlot.getValue(),
                  dateRepresentation, AlexaUtils.toAlexaList(truckNames, true)));
      }
      return builder.simpleCard("Food Trucks at " + locationSlot.getValue()).tell();
    }
  }

  private String toDate(LocalDate date) {
    if (date.equals(clock.currentDay())) {
      return "today";
    } else if (date.minusDays(1).equals(clock.currentDay())) {
      return "tomorrow";
    } else {
      return "on that date";
    }
  }
}
