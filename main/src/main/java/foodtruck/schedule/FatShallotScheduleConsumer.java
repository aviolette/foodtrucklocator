package foodtruck.schedule;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;

import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.Client;

import org.joda.time.Interval;

import foodtruck.annotations.UserAgent;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;

/**
 * @author aviolette
 * @since 11/10/18
 */
public class FatShallotScheduleConsumer implements ScheduleStrategy {

  private static final Logger log = Logger.getLogger(FatShallotScheduleConsumer.class.getName());

  private final Client client;
  private final String userAgent;
  private final FatShallotScheduleParser parser;
  private final TruckDAO truckDAO;

  @Inject
  public FatShallotScheduleConsumer(Client client, @UserAgent String userAgent, FatShallotScheduleParser parser,
      TruckDAO truckDAO) {
    this.client = client;
    this.userAgent = userAgent;
    this.parser = parser;
    this.truckDAO = truckDAO;
  }

  @Override
  public List<TruckStop> findForTime(Interval range, @Nullable Truck searchTruck) {
    if (searchTruck != null && !searchTruck.getId().equals("thefatshallot")) {
      return ImmutableList.of();
    }

    log.info("Getting fat shallot schedule");

    String document = client.resource("http://thefatshallot.com/schedule/")
        .header(HttpHeaders.USER_AGENT, userAgent)
        .get(String.class);

    Truck truck = truckDAO.findByIdOpt("thefatshallot")
        .orElse(null);

    if (truck == null) {
      log.log(Level.WARNING, "The fat shallot was not found");
      return ImmutableList.of();
    }

    return parser.parse(document, truck);
  }
}
