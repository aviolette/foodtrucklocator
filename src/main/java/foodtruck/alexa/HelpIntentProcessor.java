package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

/**
 * @author aviolette
 * @since 8/28/16
 */
class HelpIntentProcessor implements IntentProcessor {
  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    return SpeechletResponseBuilder.builder()
        .speechText(
            "Food Truck Finder.  You can ask me what trucks will be at a location or information on a specific truck.")
        .repromptText(
            "Food Truck Finder.  You can ask me what trucks will be at a location or information on a specific truck.  For example, you can say What trucks are on Clark and Monroe?  Or Where will Cheesies be for lunch today?")
        .simpleCardWithText("Food Truck Finder Help",
            "Some example things to say:\n What food trucks are at Clark and Monroe today?\n Tell me about Cheesies.\n Where will Corner Farmacy be for lunch?\nWhat are the Doughnut Vault's specials?")
        .ask();
  }
}
