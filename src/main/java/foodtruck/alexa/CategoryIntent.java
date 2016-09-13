package foodtruck.alexa;

import java.util.List;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

import static foodtruck.alexa.Conjunction.and;
import static foodtruck.util.MoreStrings.capitalize;

/**
 * @author aviolette
 * @since 9/9/16
 */
public class CategoryIntent implements IntentProcessor {

  private static final String CATEGORY_NAME = "Category";
  private static final ImmutableList<String> NATIONALITIES = ImmutableList.of("asian", "mexican", "cajun", "german",
      "greek", "jamaican", "korean", "polish", "venezuelan", "vietnamese");
  private final TruckDAO truckDAO;

  @Inject
  public CategoryIntent(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }

  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    String category = intent.getSlot(CATEGORY_NAME)
        .getValue();
    if (Strings.isNullOrEmpty(category)) {
      return SpeechletResponseBuilder.builder()
          .speechText("What keyword would you like to search for?")
          .repromptText(
              "You can search for food trucks by what type of food they sell.  For example, you can say 'What trucks sell tacos?'.  What kind of food do you want to find?")
          .ask();
    }
    List<String> trucks = FluentIterable.from(truckDAO.findActiveTrucks())
        .filter(new Truck.HasCategoryPredicate(capitalize(category)))
        .transform(Truck.TO_NAME)
        .toList();
    if (NATIONALITIES.contains(category.toLowerCase())) {
      category = capitalize(category) + " food";
    }
    String result = trucks.isEmpty() ? "There are no trucks that have " + category : String.format(
        "These trucks have %s: %s", category, AlexaUtils.toAlexaList(trucks, true, and));

    return SpeechletResponseBuilder.builder()
        .speechSSML(result)
        .simpleCard("Trucks that have " + category)
        .tell();
  }
}
