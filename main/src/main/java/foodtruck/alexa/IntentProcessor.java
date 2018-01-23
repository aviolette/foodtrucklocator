package foodtruck.alexa;

import java.util.Set;

import javax.annotation.Nullable;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.SpeechletResponse;

/**
 * @author aviolette
 * @since 8/25/16
 */
public interface IntentProcessor {
  Set<String> getSlotNames();

  SpeechletResponse process(Intent intent, @Nullable AmazonConnector connector);
}
