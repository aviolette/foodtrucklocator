package foodtruck.alexa;

import java.util.Set;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.SpeechletResponse;

/**
 * @author aviolette
 * @since 8/25/16
 */
public interface IntentProcessor {
  Set<String> getSlotNames();

  SpeechletResponse process(Intent intent, AmazonConnector connector);
}
