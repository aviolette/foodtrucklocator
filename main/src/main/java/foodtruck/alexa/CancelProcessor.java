package foodtruck.alexa;

import java.util.Set;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Context;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.collect.ImmutableSet;


/**
 * @author aviolette
 * @since 8/28/16
 */
class CancelProcessor implements IntentProcessor {
  @Override
  public Set<String> getSlotNames() {
    return ImmutableSet.of();
  }

  @Override
  public SpeechletResponse process(Intent intent, Session session, Context context) {
    return SpeechletResponseBuilder.builder().speechText("Goodbye").tell();
  }
}
