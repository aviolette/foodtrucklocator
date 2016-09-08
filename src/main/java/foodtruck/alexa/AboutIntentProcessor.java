package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Period;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 9/7/16
 */
class AboutIntentProcessor implements IntentProcessor {
  private static final String TRUCK_SLOT = "Truck";
  private final TruckDAO truckDAO;
  private final Clock clock;

  @Inject
  public AboutIntentProcessor(TruckDAO truckDAO, Clock clock) {
    this.truckDAO = truckDAO;
    this.clock = clock;
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
    Truck.Stats stats = truck.getStats();
    DateTime lastSeen = stats != null ? stats.getLastSeen() : null;
    Location whereLastSeen = stats != null ? stats.getWhereLastSeen() : null;
    String lastSeenPart;
    if (lastSeen == null || whereLastSeen == null) {
      lastSeenPart = String.format("%s has never been seen on the road.", truck.getNameInSSML());
    } else {
      Period period = new Period(lastSeen, clock.now());
      lastSeenPart = String.format("%s was last seen %d days ago at %s", truck.getNameInSSML(), period.getDays(),
          whereLastSeen.getName());
    }
    return SpeechletResponseBuilder.builder()
        .speechSSML(String.format("%s <break time=\"0.3s\" %s", truck.getDescription(), lastSeenPart))
        .simpleCard(truck.getName())
        .tell();
  }
}
