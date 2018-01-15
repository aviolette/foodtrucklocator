package foodtruck.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.servlet.SpeechletServlet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 8/25/16
 */
@Singleton
public class AlexaServlet extends SpeechletServlet {
  private static final Logger log = Logger.getLogger(AlexaServlet.class.getName());

  @Inject
  public AlexaServlet(SpeechletV2 alexaSpeechlet) {
    super();
    setSpeechlet(alexaSpeechlet);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    log.info("Alexa request received");
    super.doPost(request, response);
  }
}
