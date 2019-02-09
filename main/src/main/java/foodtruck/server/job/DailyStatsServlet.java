package foodtruck.server.job;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import foodtruck.dao.LocationLoadingCache;
import foodtruck.dao.SlackWebhookDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.Location;
import foodtruck.model.SlackWebhook;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.monitoring.CounterPublisher;
import foodtruck.time.Clock;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

/**
 * @author aviolette
 * @since 2019-01-02
 */

@Singleton
public class DailyStatsServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(DailyStatsServlet.class.getName());

  private final TruckDAO truckDAO;
  private final TruckStopDAO stopDAO;
  private final Clock clock;
  private final Provider<Queue> queueProvider;
  private final CounterPublisher publisher;
  private final SlackWebhookDAO slackDAO;
  private final LocationLoadingCache locations;

  @Inject
  public DailyStatsServlet(Clock clock, TruckStopDAO stopDAO, TruckDAO truckDAO, Provider<Queue> queueProvider,
      CounterPublisher publisher, SlackWebhookDAO slackWebhookDAO, LocationLoadingCache locations) {
    this.stopDAO = stopDAO;
    this.truckDAO = truckDAO;
    this.clock = clock;
    this.queueProvider = queueProvider;
    this.publisher = publisher;
    this.slackDAO = slackWebhookDAO;
    this.locations = locations;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LocalDate now = clock.currentDay();
    LocalDate startDate = now.minusDays(1);
    log.log(Level.INFO, "Queueing month stats generation");
    Queue queue = queueProvider.get();
    queue.add(withUrl("/cron/monthly_stop_stats_generate").param("month", String.valueOf(startDate.getMonthOfYear()))
        .param("year", String.valueOf(startDate.getYear())));
    log.info("Starting update of daily stats from: " + startDate);
    List<TruckStop> stops = stopDAO.findOverRange(null, new Interval(startDate.toDateTimeAtStartOfDay(clock.zone()),
        clock.currentDay()
            .toDateTimeAtStartOfDay(clock.zone())));
    recordTruckStats(stops);
    long uniqueTrucks = stops.stream()
        .map(stop -> stop.getTruck()
            .getId())
        .distinct()
        .count();
    long timeAtStartOfDay = now.toDateTimeAtStartOfDay(clock.zone())
        .getMillis();
    publisher.increment("daily_stops", stops.size(), timeAtStartOfDay, ImmutableMap.of());
    publisher.increment("unique_trucks", (int) uniqueTrucks, timeAtStartOfDay, ImmutableMap.of());
    slackDAO.findAll()
        .stream()
        .collect(Collectors.groupingBy(SlackWebhook::getLocationName, Collectors.counting()))
        .forEach((location, count) -> publisher.increment("slack_subscribers", (int) (long) count, timeAtStartOfDay,
            ImmutableMap.of("LOCATION", location)));
    stops.stream()
        .map(stop -> locations.findLocation(stop.getLocation()
            .getName())
            .orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(this::locality, Collectors.counting()))
        .forEach((locality, count) -> publisher.increment("stops_by_location", (int) (long) count, timeAtStartOfDay,
            ImmutableMap.of("LOCALITY", locality)));
    log.log(Level.INFO, "Updated stats for {0} stops", stops.size());
  }

  private String locality(Location location) {
    String city = location.getCity();
    if (city == null) {
      return "UNKNOWN";
    } else if ("Chicago".equals(city)) {
      return "CITY";
    } else {
      return "SUBURBS";
    }
  }

  private void recordTruckStats(List<TruckStop> stops) {
    stops.forEach(stop -> truckDAO.findByIdOpt(stop.getTruck()
        .getId())
        .ifPresent(truck -> {
          log.log(Level.INFO, "Processing stop: {0}", stop);
          Truck.Stats stats = truck.getStats();
          if (stats == null) {
            stats = new Truck.Stats.Builder()
                .build();
          }
          Truck.Stats.Builder statsBuilder = new Truck.Stats.Builder(stats);
          if (stats.getFirstSeen() == null) {
            statsBuilder.firstSeen(stop.getStartTime());
            statsBuilder.whereFirstSeen(stop.getLocation());
          }
          statsBuilder.totalStops(stats.getTotalStops() + 1);
          if (stats.getLastSeen() == null || stats.getLastSeen()
              .isBefore(stop.getEndTime())) {
            statsBuilder.lastSeen(stop.getEndTime());
            statsBuilder.whereLastSeen(stop.getLocation());
          }
          truckDAO.save(Truck.builder(truck)
              .stats(statsBuilder.build())
              .build());
        }));
  }
}
