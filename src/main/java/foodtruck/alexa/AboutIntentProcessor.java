package foodtruck.alexa;

import java.util.List;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStop;
import foodtruck.model.Url;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 9/7/16
 */
class AboutIntentProcessor implements IntentProcessor {
  static final String TRUCK_SLOT = "Truck";
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
    String truckValue = intent.getSlot(TRUCK_SLOT)
        .getValue();
    if (Strings.isNullOrEmpty(truckValue)) {
      return notFound();
    }
    Truck truck = truckDAO.findByNameOrAlias(truckValue);
    if (truck == null) {
      return notFound();
    }
    DateTime now = clock.now();
    TruckSchedule schedule = service.findStopsForDay(truck.getId(), clock.currentDay());
    String schedulePart;
    List<String> remainingStops = FluentIterable.from(schedule.getStops())
        .filter(new TruckStop.ActiveAfterPredicate(now))
        .transform(TruckStop.TO_NAME_WITH_TIME)
        .toList();
    List<String> currentStops = FluentIterable.from(schedule.getStops())
        .filter(new TruckStop.ActiveDuringPredicate(now))
        .transform(TruckStop.TO_LOCATION_NAME)
        .toList();
    if (!remainingStops.isEmpty() || !currentStops.isEmpty()) {
      String currentPart = currentStops.isEmpty() ? "" : String.format("%s is currently at %s.", truck.getNameInSSML(),
          AlexaUtils.toAlexaList(currentStops, true));
      String laterPart = remainingStops.isEmpty() ? "" : String.format("%s will be at %s.", truck.getNameInSSML(),
          AlexaUtils.toAlexaList(remainingStops, true));
      schedulePart = currentPart + " " + laterPart;
    } else {
      schedulePart = lastSeen(truck, now, schedule);
    }
    String largeIconUrl = truck.getFullsizeImage() == null ? truck.getBackgroundImage() : truck.getFullsizeImage();
    Url previewIcon = truck.getPreviewIconUrl(),
        largeIcon = largeIconUrl == null ? null : new Url(largeIconUrl);
    return SpeechletResponseBuilder.builder()
        .speechSSML(String.format("%s<break time=\"0.3s\"/>\n%s", truck.getDescription(), schedulePart.trim()))
        .imageCard(truck.getName(), largeIcon == null ? null : largeIcon.secure(),
            previewIcon == null ? null : previewIcon.secure())
        .tell();
  }

  private SpeechletResponse notFound() {
    return SpeechletResponseBuilder.builder()
        .speechText(TruckLocationIntentProcessor.TRUCK_NOT_FOUND)
        .repromptText(TruckLocationIntentProcessor.TRUCK_NOT_FOUND_REPROMPT)
        .ask();
  }

  private String lastSeen(Truck truck, DateTime now, TruckSchedule schedule) {
    DateTime lastSeen = null;
    Location whereLastSeen = null;
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
    if (lastSeen == null || whereLastSeen == null) {
      return String.format("%s has never been seen on the road.", truck.getNameInSSML());
    } else {
      String formatPart;
      LocalDate lastSeenDate = lastSeen.toLocalDate();
      if (lastSeenDate.equals(now.toLocalDate())) {
        formatPart = "today";
      } else if (lastSeenDate.equals(now.toLocalDate()
          .minusDays(1))) {
        formatPart = "yesterday";
      } else {
        Duration d = new Duration(lastSeen, now);
        formatPart = String.format("%d days ago", d.getStandardDays());
      }
      return String.format("%s was last seen %s at %s.", truck.getNameInSSML(), formatPart,
          whereLastSeen.getShortenedName());
    }
  }
}
