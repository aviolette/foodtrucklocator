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

  SpeechletResponseBuilder imageCard(String title, @Nullable String largeImage, @Nullable String smallImage) {
    ImageCard card = new ImageCard();
    card.setTitle(title);
    card.setText(scrub(speechText));
    ImageSet imageSet = new ImageSet();
    imageSet.setLargeImageUrl(largeImage);
    imageSet.setSmallImageUrl(smallImage);
    card.setImage(imageSet);
    this.card = card;
    return this;
  }

  SpeechletResponseBuilder simpleCard(String title) {
    return simpleCardWithText(title, speechText);
  }

  SpeechletResponseBuilder simpleCardWithText(String title, String bodyText) {
    SimpleCard card = new SimpleCard();
    card.setTitle(title);
    card.setContent(scrub(bodyText));
    this.card = card;
    return this;
  }

  private String scrub(String speechText) {
    return speechSSML ? speechText.replaceAll("\\<[^>]*>", "") : speechText;
  }

  SpeechletResponseBuilder speechText(String text) {
    this.speechText = text;
    this.speechSSML = false;
    return this;
  }

  SpeechletResponseBuilder speechSSML(String text) {
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
    text = text.replaceAll("\n", " ");
    if (ssml) {
      SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
      outputSpeech.setSsml("<speak>" + text + "</speak>");
      return outputSpeech;
    } else {
      PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
      outputSpeech.setText(text);
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

  SpeechletResponseBuilder useSpeechTextForReprompt() {
    repromptText = speechText;
    repromptSSML = speechSSML;
    return this;
  }
}
