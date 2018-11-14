package foodtruck.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;

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
@SuppressWarnings("UnstableApiUsage")
public class PizzaBossSuperSpecialConsumer implements ScheduleStrategy {

  private static final Logger log = Logger.getLogger(PizzaBossSuperSpecialConsumer.class.getName());

  private final GcsService cloudStorage;
  private final ICalStopConsumer consumer;
  private final TruckDAO truckDAO;

  @Inject
  public PizzaBossSuperSpecialConsumer(GcsService cloudStorage, ICalStopConsumer iCalStopConsumer, TruckDAO truckDAO) {
    this.cloudStorage = cloudStorage;
    this.consumer = iCalStopConsumer;
    this.truckDAO = truckDAO;
  }

  @Override
  public List<TruckStop> findForTime(Interval range, @Nullable Truck searchTruck) {
    if (searchTruck != null && !searchTruck.getId().equals("chipizzaboss")) {
      return ImmutableList.of();
    } else if (searchTruck == null) {
      searchTruck = truckDAO.findByIdOpt("chipizzaboss")
          .orElseThrow(() -> new RuntimeException("Chicago pizza boss not found"));
    }
    log.info("Loading pizza bosses schedule...");
    GcsFilename fileName = new GcsFilename("chicagopizzaboss", "calendar.json");
    GcsInputChannel readChannel = cloudStorage.openPrefetchingReadChannel(fileName, 0, 2097152);
    try (InputStream in = Channels.newInputStream(readChannel)) {
      String json = new String(ByteStreams.toByteArray(in), "UTF-8");
      JSONArray arr = new JSONArray(json);
      ImmutableList.Builder<TruckStop> stopBuilder = ImmutableList.builder();
      for (String link : JSONSerializer.toStringList(arr)) {
        log.log(Level.INFO, "Loading link {0}", link);
        stopBuilder.addAll(consumer.findForRange(range, searchTruck, link));
      }
      return stopBuilder.build();

    } catch (IOException | JSONException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
    return null;
  }
}
