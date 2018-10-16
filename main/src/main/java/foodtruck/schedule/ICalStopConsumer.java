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
 * @since 10/14/18
 */
public class ICalStopConsumer implements ScheduleStrategy {

  private static final Logger log = Logger.getLogger(ICalStopConsumer.class.getName());

  private final TruckDAO truckDAO;
  private final StaticConfig config;
  private final Client client;
  private final ICalStopReader reader;

  @Inject
  public ICalStopConsumer(TruckDAO truckDAO, StaticConfig config, Client client, ICalStopReader reader) {
    this.truckDAO = truckDAO;
    this.client = client;
    this.config = config;
    this.reader = reader;
  }

  @Override
  public List<TruckStop> findForTime(Interval range, @Nullable Truck truck) {
    if (truck == null) {
      return truckDAO.findTrucksWithCalendars().stream()
          .map(t -> findForTime(range, t))
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
    } else {
      if (Strings.isNullOrEmpty(truck.getIcalCalendar())) {
        return ImmutableList.of();
      }
      log.log(Level.INFO, "ICAL: {0} on {1}", new Object[]{range, truck});
      return reader.read(client.resource(truck.getIcalCalendar())
          .header(HttpHeaders.USER_AGENT, config.getUserAgent())
          .get(String.class), truck)
          .stream()
          .filter(stop -> range.contains(stop.getStartTime()) || range.contains(stop.getEndTime()))
          .collect(Collectors.toList());

    }
  }
}
