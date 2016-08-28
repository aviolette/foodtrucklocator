package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;


/**
 * @author aviolette
 * @since 8/28/16
 */
public class CancelProcessor implements IntentProcessor {
  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
    outputSpeech.setText("Goodbye");

    return SpeechletResponse.newTellResponse(outputSpeech);
  }
}
