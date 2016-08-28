package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

/**
 * @author aviolette
 * @since 8/28/16
 */
public class HelpIntentProcessor implements IntentProcessor {
  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    String speechText = "Food Truck Finder.  You can ask me what trucks will be at a location or where a specific truck is today";
    SimpleCard card = new SimpleCard();
    card.setTitle("Food Truck Finder");
    card.setContent(speechText);
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(speechText);
    Reprompt reprompt = new Reprompt();
    reprompt.setOutputSpeech(speech);
    return SpeechletResponse.newAskResponse(speech, reprompt, card);
  }
}
