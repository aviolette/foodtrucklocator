package foodtruck.alexa;

import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;

/**
 * @author aviolette
 * @since 8/25/16
 */
public class FTFSpeechlet implements Speechlet {
  @Override
  public void onSessionStarted(SessionStartedRequest sessionStartedRequest, Session session) throws SpeechletException {

  }

  @Override
  public SpeechletResponse onLaunch(LaunchRequest launchRequest, Session session) throws SpeechletException {
    return null;
  }

  @Override
  public SpeechletResponse onIntent(IntentRequest intentRequest, Session session) throws SpeechletException {
    String speechText = "G'day mate";

    // Create the Simple card content.
    SimpleCard card = new SimpleCard();
    card.setTitle("HelloWorld");
    card.setContent(speechText);

    // Create the plain text output.
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(speechText);

    return SpeechletResponse.newTellResponse(speech, card);
  }

  @Override
  public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {

  }
}
