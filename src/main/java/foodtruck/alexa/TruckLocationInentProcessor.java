package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;

/**
 * @author aviolette
 * @since 8/25/16
 */
public class TruckLocationInentProcessor implements IntentProcessor {
  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    SimpleCard card = new SimpleCard();
    card.setTitle("Where is Food Truck");
    String speechText = "Hoos Foos";
    card.setContent(speechText);
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(speechText);
    return SpeechletResponse.newTellResponse(speech, card);
  }
}
