package foodtruck.schedule;

import java.net.MalformedURLException;
import java.util.ArrayList;
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
 * @since 10/16/18
 */
public class SquarespaceCalendarConsumer implements ScheduleStrategy {

  private static final Logger log = Logger.getLogger(SquarespaceCalendarConsumer.class.getName());

  private final TruckDAO truckDAO;
  private final Client client;
  private final StaticConfig config;
  private final ICalStopConsumer consumer;
  private final SquarespaceLinkExtractor extractor;

  @Inject
  public SquarespaceCalendarConsumer(TruckDAO truckDAO, Client client, StaticConfig config,
      SquarespaceLinkExtractor extractor, ICalStopConsumer iCalStopConsumer) {
    this.truckDAO = truckDAO;
    this.client = client;
    this.config = config;
    this.extractor = extractor;
    this.consumer = iCalStopConsumer;
  }

  @Override
  public List<TruckStop> findForTime(Interval range, @Nullable Truck truck) {
    if (truck == null) {
      return truckDAO.findTruckWithSquarespaceCalendars().stream()
          .map(t -> findForTime(range, t))
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
    } else {
      if (Strings.isNullOrEmpty(truck.getSquarespaceCalendar())) {
        return ImmutableList.of();
      }
      try {
        List<String> icalLinks = new ArrayList<>(extractor.findLinks(client.resource(truck.getSquarespaceCalendar())
            .header(HttpHeaders.USER_AGENT, config.getUserAgent())
            .get(String.class), truck));

        ImmutableList.Builder<TruckStop> stopBuilder = ImmutableList.builder();
        for (String link : icalLinks) {
          log.log(Level.INFO, "Loading link {0}", link);
          stopBuilder.addAll(consumer.findForRange(range, truck, link));
        }
      } catch (MalformedURLException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }

      return ImmutableList.of();

    }
  }
}
