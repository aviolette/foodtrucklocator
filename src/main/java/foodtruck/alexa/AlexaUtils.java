package foodtruck.alexa;

import java.util.List;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.FluentIterable;

/**
 * @author aviolette
 * @since 8/27/16
 */
class AlexaUtils {
  private static final Joiner JOINER = Joiner.on(", ");
  private static final Joiner JOINER_WITH_PAUSE = Joiner.on(",<break time=\"0.3s\"/> ");

  static String toAlexaList(List<String> list, boolean insertPause) {
    return toAlexaList(list, insertPause, Conjunction.and);
  }

  static String toAlexaList(List<String> list, boolean insertPause, Conjunction conjunction) {
    if (list.isEmpty()) {
      return "";
    }
    String rac = list.get(list.size() - 1);
    if (list.size() == 1) {
      return rac;
    }
    String rdc;
    if (insertPause) {
      rdc = JOINER_WITH_PAUSE.join(list.subList(0, list.size() - 1));
    } else {
      rdc = JOINER.join(list.subList(0, list.size() - 1));
    }
    String oxfordComma = (list.size() == 2) ? "" : ",";
    return rdc + oxfordComma + " " + conjunction.toString() + " " + rac;
  }

  static String intentToString(Intent intent) {
    return MoreObjects.toStringHelper(intent)
        .add("name", intent.getName())
        .add("slots", FluentIterable.from(intent.getSlots()
            .values())
            .transform(new Function<Slot, String>() {
              @Override
              public String apply(Slot input) {
                return MoreObjects.toStringHelper(input)
                    .add("slot", input.getName())
                    .add("value", input.getValue())
                    .toString();
              }
            })
            .join(Joiner.on("\n")))
        .toString();
  }

  static String speechletResponseToString(SpeechletResponse response) {
    String outputSpeech = response.getOutputSpeech() == null ? null : outputSpeech(response.getOutputSpeech());
    String card = response.getCard() == null ? null : cardToString(response.getCard());
    String reprompt = response.getReprompt() == null ? null : repromptToString(response.getReprompt());
    return MoreObjects.toStringHelper(response)
        .add("speech", outputSpeech)
        .add("reprompt", reprompt)
        .add("card", card)
        .toString();
  }

  private static String repromptToString(Reprompt reprompt) {
    return outputSpeech(reprompt.getOutputSpeech());
  }

  private static String cardToString(Card card) {
    if (card instanceof SimpleCard) {
      SimpleCard simpleCard = (SimpleCard) card;
      return MoreObjects.toStringHelper(card)
          .add("title", simpleCard.getTitle())
          .add("content", simpleCard.getContent())
          .toString();
    } else if (card instanceof ImageCard) {
      ImageCard imageCard = (ImageCard) card;
      return MoreObjects.toStringHelper(imageCard)
          .add("title", imageCard.getTitle())
          .add("text", imageCard.getText())
          .add("image", MoreObjects.toStringHelper(imageCard.getImage())
              .add("small", imageCard.getImage()
                  .getSmallImageUrl())
              .add("large", imageCard.getImage()
                  .getLargeImageUrl()))
          .toString();
    }
    return null;
  }

  private static String outputSpeech(OutputSpeech outputSpeech) {
    if (outputSpeech instanceof PlainTextOutputSpeech) {
      return plainText((PlainTextOutputSpeech) outputSpeech);
    } else if (outputSpeech instanceof SsmlOutputSpeech) {
      return ssml((SsmlOutputSpeech) outputSpeech);
    }
    return null;
  }

  private static String ssml(SsmlOutputSpeech outputSpeech) {
    return outputSpeech.getSsml();
  }

  private static String plainText(PlainTextOutputSpeech outputSpeech) {
    return outputSpeech.getText();
  }
}
