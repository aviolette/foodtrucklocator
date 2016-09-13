package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import foodtruck.dao.DailyDataDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.DailyData;
import foodtruck.model.Truck;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 8/31/16
 */
class SpecialIntentProcessor implements IntentProcessor {
  static final String TRUCK_SLOT = "Truck";
  private final DailyDataDAO dailyDataDAO;
  private final TruckDAO truckDAO;
  private final Clock clock;

  @Inject
  public SpecialIntentProcessor(DailyDataDAO dailyDataDAO, TruckDAO truckDAO, Clock clock) {
    this.dailyDataDAO = dailyDataDAO;
    this.truckDAO = truckDAO;
    this.clock = clock;
  }

  static String specialsText(Truck truck, DailyData dailyData) {
    String speechText;
    if (dailyData == null || !dailyData.hasSpecials()) {
      speechText = String.format("There are no specials for %s today", truck.getNameInSSML());
    } else if (dailyData.getSpecials()
        .size() == 1) {
      DailyData.SpecialInfo specialInfo = dailyData.getSpecials()
          .iterator()
          .next();
      String soldOutText = specialInfo.isSoldOut() ? " but it appears to be sold out" : "";
      speechText = String.format("%s's special for today is %s%s.", truck.getNameInSSML(), specialInfo.getSpecial(),
          soldOutText);
    } else {
      speechText = String.format("%s's specials for today are %s", truck.getNameInSSML(), AlexaUtils.toAlexaList(
          FluentIterable.from(dailyData.getSpecials())
              .transform(DailyData.TO_NAME)
              .toList(), true));
    }
    return speechText;
  }

  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    String truckName = intent.getSlot(TRUCK_SLOT)
        .getValue();
    if (Strings.isNullOrEmpty(truckName)) {
      return notFound();
    }
    Truck truck = truckDAO.findByName(intent.getSlot(TRUCK_SLOT)
        .getValue());
    if (truck == null) {
      return notFound();
    }
    DailyData dailyData = dailyDataDAO.findByTruckAndDay(truck.getId(), clock.currentDay());
    String speechText = specialsText(truck, dailyData);
    return SpeechletResponseBuilder.builder()
        .speechSSML(speechText)
        .tell();
  }

  private SpeechletResponse notFound() {
    return SpeechletResponseBuilder.builder()
        .speechText(TruckLocationIntentProcessor.TRUCK_NOT_FOUND)
        .useSpeechTextForReprompt()
        .ask();
  }
}
