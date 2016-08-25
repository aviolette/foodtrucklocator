package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import com.google.inject.Inject;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 8/25/16
 */
public class LocationIntentProcessor implements IntentProcessor {
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

    Slot slot = intent.getSlot("Location");
    Location location = locator.locate(slot.getValue(), GeolocationGranularity.NARROW);

//    service.findStopsAtLocationOverRange(location, new Interval(clock.now(), clock.timeAt(23, 59)));


    String speechText = "G'day mate: " + location.getName();

    // Create the Simple card content.
    SimpleCard card = new SimpleCard();
    card.setTitle("HelloWorld");
    card.setContent(speechText);

    // Create the plain text output.
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(speechText);

    return SpeechletResponse.newTellResponse(speech, card);
  }
}
