package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

/**
 * @author aviolette
 * @since 8/25/16
 */
interface IntentProcessor {
  SpeechletResponse process(Intent intent, Session session);
}
