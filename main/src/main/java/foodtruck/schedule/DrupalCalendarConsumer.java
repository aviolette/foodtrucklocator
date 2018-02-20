package foodtruck.schedule;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.ws.rs.core.HttpHeaders;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;

import org.joda.time.Interval;

import foodtruck.dao.TruckDAO;
import foodtruck.model.StaticConfig;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;

/**
 * @author aviolette
 * @since 2/18/18
 */
public class DrupalCalendarConsumer implements ScheduleStrategy {

  private static final Logger log = Logger.getLogger(DrupalCalendarConsumer.class.getName());
  private final DrupalCalendarStopReader reader;
  private final StaticConfig config;
  private final Client client;
  private final TruckDAO truckDAO;

  @Inject
  public DrupalCalendarConsumer(DrupalCalendarStopReader reader, StaticConfig config, Client client,
      TruckDAO truckDAO) {
    this.reader = reader;
    this.config = config;
    this.client = client;
    this.truckDAO = truckDAO;
  }

  @Override
  public List<TruckStop> findForTime(Interval range, @Nullable Truck truck) {
    if (truck == null) {
      return truckDAO.findTruckWithDrupalCalendars().stream()
          .map(t -> findForTime(range, t))
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
    } else {
      log.log(Level.INFO, "DRUPAL: {0} on {1}", new Object[]{range, truck});
      if (Strings.isNullOrEmpty(truck.getDrupalCalendar())) {
        return ImmutableList.of();
      }
      return reader.read(client.resource(truck.getDrupalCalendar())
          .header(HttpHeaders.USER_AGENT, config.getUserAgent())
          .get(String.class), truck)
          .stream()
          .filter(stop -> range.contains(stop.getStartTime()) || range.contains(stop.getEndTime()))
          .collect(Collectors.toList());
    }
  }
}
