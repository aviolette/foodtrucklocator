package foodtruck.server.resources;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import com.google.common.base.Throwables;

/**
 * @author aviolette
 * @since 8/25/16
 */
@Path("/amazonalexa")
public class AlexaResource {

  @GET
  public String getRequest() {
    String speechText = "Hello world";

    // Create the Simple card content.
    SimpleCard card = new SimpleCard();
    card.setTitle("HelloWorld");
    card.setContent(speechText);

    // Create the plain text output.
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(speechText);

    SpeechletResponse response = SpeechletResponse.newTellResponse(speech, card);
    SpeechletResponseEnvelope envelope = new SpeechletResponseEnvelope();
    envelope.setResponse(response);
    try {
      return envelope.toJsonString();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
}
