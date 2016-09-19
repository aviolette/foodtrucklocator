package foodtruck.alexa;

import java.util.Set;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import org.joda.time.DateTime;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStop;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

import static foodtruck.model.TruckStop.TO_LOCATION_NAME;

/**
 * @author aviolette
 * @since 8/25/16
 */
class TruckLocationIntentProcessor implements IntentProcessor {
  static final String SLOT_TRUCK = "Truck";
  static final String SLOT_TIME_OF_DAY = "TimeOfDay";
  static final String TRUCK_NOT_FOUND = "Sorry, I did not recognize that food truck.  Which food truck would you like to know about?";
  static final String TRUCK_NOT_FOUND_REPROMPT = "I currently provide information for food trucks that operate in and around Chicago.  Which food truck would you like to know about?";
  private final FoodTruckStopService service;
  private final TruckDAO truckDAO;
  private final Clock clock;

  @Inject
  public TruckLocationIntentProcessor(FoodTruckStopService service, TruckDAO truckDAO, Clock clock) {
    this.service = service;
    this.truckDAO = truckDAO;
    this.clock = clock;
  }

  @Override
  public Set<String> getSlotNames() {
    return ImmutableSet.of(SLOT_TRUCK, SLOT_TIME_OF_DAY);
  }

  @Override
  public SpeechletResponse process(Intent intent, Session session) {
    String truckName = intent.getSlot(SLOT_TRUCK)
        .getValue();
    if (Strings.isNullOrEmpty(truckName)) {
      return notFound();
    }
    Truck truck = truckDAO.findByNameOrAlias(truckName);
    if (truck == null) {
      return notFound();
    } else {
      String when = intent.getSlot(SLOT_TIME_OF_DAY).getValue();
      return SpeechletResponseBuilder.builder()
          .speechText(speech(truck, when, TimeOfDay.fromValue(when)))
          .simpleCard(truck.getName())
          .tell();
    }
  }

  private SpeechletResponse notFound() {
    return SpeechletResponseBuilder.builder()
        .speechText(TRUCK_NOT_FOUND)
        .useSpeechTextForReprompt()
        .ask();
  }

  private String speech(Truck truck, String when, TimeOfDay tod) {
    String speechText, currentStops = "", laterStops = "";
    TruckSchedule schedule = service.findStopsForDay(truck.getId(), clock.currentDay());
    final DateTime requestTime = tod.requestTime(clock);
    DateTime now = clock.now();
    boolean considerLater = true;
    if (tod.isOver(now) && tod.isSpecific()) {
      currentStops = AlexaUtils.toAlexaList(FluentIterable.from(schedule.getStops())
          .filter(new TruckStop.ActiveDuringPredicate(requestTime))
          .transform(TO_LOCATION_NAME)
          .toList(), false);
      considerLater = false;
    } else if (tod.isApplicableNow(now)) {
      currentStops = AlexaUtils.toAlexaList(FluentIterable.from(schedule.getStops())
          .filter(new TruckStop.ActiveDuringPredicate(now))
          .transform(TO_LOCATION_NAME)
          .toList(), false);
    }
    if (considerLater && tod.isApplicableAfter(requestTime)) {
      laterStops = AlexaUtils.toAlexaList(FluentIterable.from(schedule.getStops())
          .filter(tod.filterFuture(now))
          .transform(TruckStop.TO_NAME_WITH_TIME)
          .toList(), false);
    }
    if (currentStops.length() > 0 && laterStops.isEmpty()) {
      String verbPhrase = tod.isOver(now) ? "was" : "is currently";
      String qualifier = tod.isOver(now) ? " for " + tod.toString().toLowerCase() : "";
      speechText = String.format("%s %s at %s%s", truck.getName(), verbPhrase, currentStops, qualifier);
    } else if (currentStops.isEmpty() && laterStops.length() > 0) {
      speechText = String.format("%s will be at %s", truck.getName(), laterStops);
    } else if (currentStops.length() > 0 && laterStops.length() > 0) {
      speechText = String.format("%s is currently at %s and will be at %s", truck.getName(), currentStops, laterStops);
    } else if ("now".equals(when)) {
      speechText = String.format("%s is not currently on the road", truck.getName());
    } else {
      speechText = String.format("%s is not on the road for the remainder of the day", truck.getName());
    }
    return speechText;
  }

  private enum TimeOfDay {
    TODAY {
      @Override
      public boolean isApplicableNow(DateTime dateTime) {
        return true;
      }

      @Override
      public boolean isApplicableAfter(DateTime now) {
        return true;
      }
    }, NOW {
      @Override
      public boolean isApplicableNow(DateTime dateTime) {
        return true;
      }

      @Override
      public boolean isSpecific() {
        return true;
      }
    }, LATER {
      @Override
      public boolean isApplicableNow(DateTime dateTime) {
        return false;
      }

      @Override
      public boolean isApplicableAfter(DateTime now) {
        return true;
      }
    }, LUNCH {
      @Override
      public boolean isApplicableNow(DateTime dateTime) {
        int hourOfDay = dateTime.getHourOfDay();
        return hourOfDay >= 11 && hourOfDay < 14;
      }

      @Override
      public boolean isSpecific() {
        return true;
      }

      @Override
      public DateTime requestTime(Clock clock) {
        return clock.timeAt(12, 0);
      }

      @Override
      public boolean isApplicableAfter(DateTime now) {
        int hourOfDay = now.getHourOfDay();
        return hourOfDay < 14;
      }

      @Override
      public boolean isOver(DateTime now) {
        return now.getHourOfDay() >= 14;
      }

      @Override
      public Predicate<TruckStop> filterFuture(DateTime now) {
        return new TruckStop.ActiveDuringPredicate(now.withTime(12, 0, 0, 0));
      }
    }, ELEVENSES {
      @Override
      public boolean isApplicableNow(DateTime dateTime) {
        int hourOfDay = dateTime.getHourOfDay();
        return hourOfDay >= 10 && hourOfDay < 12;
      }

      @Override
      public DateTime requestTime(Clock clock) {
        return clock.timeAt(11, 0);
      }

      @Override
      public boolean isSpecific() {
        return true;
      }

      @Override
      public boolean isApplicableAfter(DateTime now) {
        int hourOfDay = now.getHourOfDay();
        return hourOfDay < 12;
      }

      @Override
      public boolean isOver(DateTime now) {
        return now.getHourOfDay() >= 12;
      }
    }, BREAKFAST {
      @Override
      public boolean isApplicableNow(DateTime dateTime) {
        int hourOfDay = dateTime.getHourOfDay();
        return hourOfDay >= 5 && hourOfDay < 11;
      }

      @Override
      public DateTime requestTime(Clock clock) {
        return clock.timeAt(8, 0);
      }

      @Override
      public boolean isOver(DateTime now) {
        return now.getHourOfDay() >= 11;
      }

      @Override
      public boolean isSpecific() {
        return true;
      }
    }, TONIGHT {
      @Override
      public boolean isApplicableNow(DateTime dateTime) {
        int hourOfDay = dateTime.getHourOfDay();
        return hourOfDay >= 16 && hourOfDay < 25;
      }

      @Override
      public DateTime requestTime(Clock clock) {
        return clock.timeAt(17, 0);
      }

      @Override
      public boolean isApplicableAfter(DateTime now) {
        int hourOfDay = now.getHourOfDay();
        return hourOfDay < 20;
      }

      @Override
      public boolean isOver(DateTime now) {
        return now.getHourOfDay() > 20;
      }

      @Override
      public boolean isSpecific() {
        return true;
      }
    }, DINNER {
      @Override
      public boolean isApplicableNow(DateTime dateTime) {
        int hourOfDay = dateTime.getHourOfDay();
        return hourOfDay >= 16 && hourOfDay < 20;
      }

      @Override
      public DateTime requestTime(Clock clock) {
        return clock.timeAt(17, 0);
      }

      @Override
      public boolean isApplicableAfter(DateTime now) {
        int hourOfDay = now.getHourOfDay();
        return hourOfDay < 20;
      }

      @Override
      public boolean isOver(DateTime now) {
        return now.getHourOfDay() > 20;
      }

      @Override
      public boolean isSpecific() {
        return true;
      }
    };

    public static TimeOfDay fromValue(String keyword) {
      if (Strings.isNullOrEmpty(keyword)) {
        return TODAY;
      } else if (keyword.startsWith("for ")) {
        return valueOf(keyword.substring(4).toUpperCase());
      } else {
        return valueOf(keyword.toUpperCase());
      }
    }

    public boolean isOver(DateTime now) {
      return false;
    }

    public boolean isApplicableAfter(DateTime now) {
      return false;
    }

    public abstract boolean isApplicableNow(DateTime dateTime);

    public DateTime requestTime(Clock clock) {
      return clock.now();
    }

    public boolean isSpecific() {
      return false;
    }

    public Predicate<TruckStop> filterFuture(DateTime now) {
      return new TruckStop.ActiveAfterPredicate(now);
    }
  }
}
