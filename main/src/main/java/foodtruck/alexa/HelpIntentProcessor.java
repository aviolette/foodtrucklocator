package foodtruck.alexa;

import java.util.Set;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.collect.ImmutableSet;

/**
 * @author aviolette
 * @since 8/28/16
 */
class HelpIntentProcessor implements IntentProcessor {
  @Override
  public Set<String> getSlotNames() {
    return ImmutableSet.of();
  }

  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    return SpeechletResponseBuilder.builder()
        .speechText(
            "Food Truck Finder.  You can ask me what trucks will be at a location or information on a specific truck.")
        .repromptText(
            "Food Truck Finder.  You can ask me what trucks will be at a location or information on a specific truck.  For example, you can say What trucks are on Clark and Monroe?  Or Where will Cheesies be for lunch today?  What would you like to find?")
        .simpleCardWithText("Food Truck Finder Help",
            "Some example things to say:\n\nWhat food trucks are at Clark and Monroe today?\n\nTell me about Cheesies.\n\nWhere will Corner Farmacy be for lunch?\n\nWhat are the Doughnut Vault's specials?")
        .ask();
  }
}
