package foodtruck.schedule;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.Interval;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.server.resources.json.JSONSerializer;

/**
 * @author aviolette
 * @since 11/13/18
 */
public class PizzaBossSuperSpecialConsumer implements ScheduleStrategy {

  private static final Logger log = Logger.getLogger(PizzaBossSuperSpecialConsumer.class.getName());

  private final ICalStopConsumer consumer;
  private final TruckDAO truckDAO;
  private final Client client;

  @Inject
  public PizzaBossSuperSpecialConsumer(ICalStopConsumer iCalStopConsumer, TruckDAO truckDAO, Client client) {
    this.consumer = iCalStopConsumer;
    this.truckDAO = truckDAO;
    this.client = client;
  }

  @Override
  public List<TruckStop> findForTime(Interval range, @Nullable Truck searchTruck) {
    if (searchTruck != null && !searchTruck.getId()
        .equals("chipizzaboss")) {
      return ImmutableList.of();
    } else if (searchTruck == null) {
      searchTruck = truckDAO.findByIdOpt("chipizzaboss")
          .orElseThrow(() -> new RuntimeException("Chicago pizza boss not found"));
    }
    log.info("Loading pizza bosses schedule...");
    ImmutableList.Builder<TruckStop> stopBuilder = ImmutableList.builder();
    for (int i=0; i < 3; i++) {
      try {
        JSONArray arr = client.resource(
            "https://us-central1-chicagofoodtrucklocator.cloudfunctions.net/chicagopizzaboss-schedule")
            .get(JSONArray.class);
        for (String link : JSONSerializer.toStringList(arr)) {
          log.log(Level.INFO, "Loading link {0}", link);
          stopBuilder.addAll(consumer.findForRange(range, searchTruck, link));
        }
      } catch (JSONException | UniformInterfaceException | ClientHandlerException e) {
        log.log(Level.WARNING, e.getMessage(), e);
      }
    }
    return stopBuilder.build();
  }
}
