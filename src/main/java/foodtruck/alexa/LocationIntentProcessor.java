package foodtruck.alexa;

import java.util.Set;
import java.util.logging.Logger;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Interval;

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
public class LocationIntentProcessor implements IntentProcessor {
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

    Slot slot = intent.getSlot("Location");
    Location location = locator.locate(slot.getValue(), GeolocationGranularity.NARROW);
    String speechText;
    if (location == null) {
      log.severe("Could not find location " + slot.getValue() + " that is specified in alexa");
      speechText = "There are no trucks at this location";
    } else {
      DateTime start = clock.now();
      DateTime end = clock.timeAt(23, 59);
      log.info("Requested food trucks at " + location.getName() + " " + start + " " + end);
      Set<String> truckNames = FluentIterable.from(
          service.findStopsAtLocationOverRange(location, new Interval(clock.now(), clock.timeAt(23, 59))))
          .transform(new Function<TruckStop, String>() {
            public String apply(TruckStop input) {
              return input.getTruck().getName();
            }
          })
          .toSet();
      speechText = "These food trucks are at " + slot.getValue() + " today: " + Joiner.on(", ").join(truckNames);
    }

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
