package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStop;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 9/7/16
 */
class AboutIntentProcessor implements IntentProcessor {
  private static final String TRUCK_SLOT = "Truck";
  private final TruckDAO truckDAO;
  private final Clock clock;
  private final FoodTruckStopService service;

  @Inject
  public AboutIntentProcessor(TruckDAO truckDAO, Clock clock, FoodTruckStopService service) {
    this.truckDAO = truckDAO;
    this.clock = clock;
    this.service = service;
  }

  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    Truck truck = truckDAO.findByNameOrAlias(intent.getSlot(TRUCK_SLOT)
        .getValue());
    if (truck == null) {
      return SpeechletResponseBuilder.builder()
          .speechText(TruckLocationIntentProcessor.TRUCK_NOT_FOUND)
          .useSpeechTextForReprompt()
          .ask();
    }
    DateTime lastSeen = null, now = clock.now();
    Location whereLastSeen = null;
    TruckSchedule schedule = service.findStopsForDay(truck.getId(), clock.currentDay());
    TruckStop lastActive = schedule.findLastActive(now);
    Truck.Stats stats = truck.getStats();
    if (lastActive != null) {
      lastSeen = (lastActive.getEndTime()
          .isAfter(now)) ? now : lastActive.getEndTime();
      whereLastSeen = lastActive.getLocation();
    } else if (stats != null) {
      lastSeen = stats.getLastSeen();
      whereLastSeen = stats.getWhereLastSeen();
    }
    String lastSeenPart;
    if (lastSeen == null || whereLastSeen == null) {
      lastSeenPart = String.format("%s has never been seen on the road.", truck.getNameInSSML());
    } else {
      String formatPart;
      if (lastSeen.toLocalDate()
          .equals(now.toLocalDate())) {
        formatPart = "today";
      } else if (lastSeen.toLocalDate()
          .equals(now.toLocalDate()
              .minusDays(1))) {
        formatPart = "yesterday";
      } else {
        Duration d = new Duration(lastSeen, now);
        formatPart = String.format("%d days ago", d.getStandardDays());
      }
      lastSeenPart = String.format("%s was last seen %s at %s", truck.getNameInSSML(), formatPart,
          whereLastSeen.getShortenedName());
    }
    return SpeechletResponseBuilder.builder()
        .speechSSML(String.format("%s <break time=\"0.3s\"/> %s", truck.getDescription(), lastSeenPart))
        .imageCard(truck.getName(), truck.getPreviewIcon(), truck.getIconUrl())
        .tell();
  }
}
