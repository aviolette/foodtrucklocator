package foodtruck.alexa;

import javax.annotation.Nullable;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;

/**
 * @author aviolette
 * @since 8/29/16
 */
class SpeechletResponseBuilder {
  private String speechText, repromptText;
  private boolean speechSSML, repromptSSML;
  private @Nullable Card card;

  public static SpeechletResponseBuilder builder() {
    return new SpeechletResponseBuilder();
  }

  SpeechletResponseBuilder card(@Nullable Card card) {
    this.card = card;
    return this;
  }

  SpeechletResponseBuilder simpleCard(String title) {
    SimpleCard card = new SimpleCard();
    card.setTitle(title);
    card.setContent(speechText);
    this.card = card;
    return this;
  }

  SpeechletResponseBuilder speechText(String text) {
    this.speechText = text;
    this.speechSSML = false;
    return this;
  }

  public SpeechletResponseBuilder speechSSML(String text) {
    this.speechText = text;
    this.speechSSML = true;
    return this;
  }

  SpeechletResponseBuilder repromptText(String text) {
    this.repromptText = text;
    this.repromptSSML = false;
    return this;
  }

  public SpeechletResponseBuilder repromptSSML(String text) {
    this.repromptText = text;
    this.repromptSSML = true;
    return this;
  }

  private OutputSpeech buildOutput(String text, boolean ssml) {
    if (ssml) {
      SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
      outputSpeech.setSsml(speechText);
      return outputSpeech;
    } else {
      PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
      outputSpeech.setText(speechText);
      return outputSpeech;
    }
  }

  SpeechletResponse ask() {
    Reprompt reprompt = new Reprompt();
    reprompt.setOutputSpeech(buildOutput(repromptText, repromptSSML));
    SpeechletResponse response = SpeechletResponse.newAskResponse(buildOutput(speechText, speechSSML), reprompt);
    if (card != null) {
      response.setCard(card);
    }
    return response;
  }

  SpeechletResponse tell() {
    if (card != null) {
      return SpeechletResponse.newTellResponse(buildOutput(speechText, speechSSML), card);
    } else {
      return SpeechletResponse.newTellResponse(buildOutput(speechText, speechSSML));
    }
  }

  public SpeechletResponseBuilder useSpeechTextForReprompt() {
    repromptText = speechText;
    repromptSSML = speechSSML;
    return this;
  }
}
