package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

/**
 * @author aviolette
 * @since 8/28/16
 */
class HelpIntentProcessor implements IntentProcessor {
  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    return SpeechletResponseBuilder.builder()
        .speechText(
            "Food Truck Finder.  You can ask me what trucks will be at a location or where a specific truck is today")
        .useSpeechTextForReprompt()
        .simpleCard("Food Truck Finder Help")
        .ask();
  }
}
