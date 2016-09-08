package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.inject.Inject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 9/7/16
 */
class AboutIntentProcessor implements IntentProcessor {
  private static final String TRUCK_SLOT = "Truck";
  private final TruckDAO truckDAO;

  @Inject
  public AboutIntentProcessor(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }

  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    Truck truck = truckDAO.findByName(intent.getSlot(TRUCK_SLOT)
        .getValue());
    if (truck == null) {
      return SpeechletResponseBuilder.builder()
          .speechText(TruckLocationIntentProcessor.TRUCK_NOT_FOUND)
          .useSpeechTextForReprompt()
          .ask();
    }
    return SpeechletResponseBuilder.builder()
        .speechText(truck.getDescription())
        .simpleCard(truck.getName())
        .tell();
  }
}
