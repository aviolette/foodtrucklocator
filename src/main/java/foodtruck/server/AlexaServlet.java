package foodtruck.server;

import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.servlet.SpeechletServlet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 8/25/16
 */
@Singleton
public class AlexaServlet extends SpeechletServlet {

  @Inject
  public AlexaServlet(Speechlet alexaSpeechlet) {
    super();
    setSpeechlet(alexaSpeechlet);
  }
}
