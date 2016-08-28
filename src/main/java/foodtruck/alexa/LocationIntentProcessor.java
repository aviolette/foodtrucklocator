package foodtruck.alexa;

import java.util.List;
import java.util.logging.Logger;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import com.google.common.base.Function;
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
    StringBuilder speechTextBuilder = new StringBuilder();
    LocalDate requestDate = "tomorrow".equals(intent.getSlot(SLOT_WHEN).getValue()) ? clock.currentDay()
        .plusDays(1) : clock.currentDay();
    boolean inFuture = requestDate.isAfter(clock.currentDay());
    String dateRepresentation = toDate(requestDate);
    String noTrucks = "There are no trucks at " + locationSlot.getValue() + " " + dateRepresentation;
    if (location == null) {
      log.severe("Could not find location " + locationSlot.getValue() + " that is specified in alexa");
      speechTextBuilder.append(noTrucks);
    } else {
      List<TruckStop> stops = service.findStopsNearALocation(location, requestDate);
      List<String> truckNames = FluentIterable.from(stops).transform(new Function<TruckStop, String>() {
        public String apply(TruckStop input) {
          return input.getTruck().getName();
        }
      }).toList();
      int count = stops.size();
      String futurePhrase = inFuture ? "scheduled to be " : "";
      if (count == 0) {
        speechTextBuilder.append(noTrucks);
      } else if (count == 1) {
        speechTextBuilder.append(truckNames.get(0))
            .append(" is the only food truck ")
            .append(futurePhrase)
            .append("at ")
            .append(locationSlot.getValue())
            .append(" ")
            .append(dateRepresentation);
      } else if (count == 2) {
        speechTextBuilder.append(truckNames.get(0))
            .append(" and ")
            .append(truckNames.get(1))
            .append(" are ")
            .append(futurePhrase)
            .append("at ")
            .append(locationSlot.getValue())
            .append(" ")
            .append(dateRepresentation);
      } else {
        String joined = AlexaUtils.toAlexaList(truckNames);
        speechTextBuilder.append("There are ")
            .append(count)
            .append(" trucks ")
            .append(futurePhrase)
            .append("at ")
            .append(locationSlot.getValue())
            .append(" ")
            .append(dateRepresentation)
            .append(": ")
            .append(joined);
      }
    }
    SimpleCard card = new SimpleCard();
    card.setTitle("Food Trucks at " + locationSlot.getValue());
    String speechText = speechTextBuilder.toString();
    card.setContent(speechText);
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(speechText);
    return SpeechletResponse.newTellResponse(speech, card);
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
