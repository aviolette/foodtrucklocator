package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;


/**
 * @author aviolette
 * @since 8/28/16
 */
class CancelProcessor implements IntentProcessor {
  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    return SpeechletResponseBuilder.builder().speechText("Goodbye").tell();
  }
}
