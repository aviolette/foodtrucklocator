package foodtruck.alexa;

import java.util.List;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.util.MoreStrings;

import static foodtruck.alexa.Conjunction.and;

/**
 * @author aviolette
 * @since 9/9/16
 */
public class CategoryIntent implements IntentProcessor {

  private static final String CATEGORY_NAME = "Category";
  private final TruckDAO truckDAO;

  @Inject
  public CategoryIntent(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }

  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    String category = intent.getSlot(CATEGORY_NAME)
        .getValue();
    if (category == null) {
      // TODO: help
      return SpeechletResponseBuilder.builder()
          .speechText("You can find trucks by category.  What category would you like to search for?")
          .repromptText("For example, you can say 'What trucks have tacos?'.  What would you like to find?")
          .ask();
    }
    List<String> trucks = FluentIterable.from(truckDAO.findActiveTrucks())
        .filter(new Truck.HasCategoryPredicate(MoreStrings.capitalize(category)))
        .transform(Truck.TO_NAME)
        .toList();
    String result = trucks.isEmpty() ? "There are no trucks that have " + category : String.format(
        "These trucks have %s: %s", category, AlexaUtils.toAlexaList(trucks, true, and));
    return SpeechletResponseBuilder.builder()
        .simpleCard("Trucks that have " + category)
        .speechSSML(result)
        .tell();
  }
}
