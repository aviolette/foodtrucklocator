package foodtruck.datalake;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.server.resources.json.JSONWriter;
import foodtruck.server.resources.json.TruckStopWriter;
import foodtruck.server.resources.json.TruckWriter;
import foodtruck.storage.StorageService;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2018-12-29
 */
public class DataExportServiceImpl implements DataExportService {

  private static final Logger log = Logger.getLogger(DataExportServiceImpl.class.getName());

  private final StorageService storageService;
  private final TruckDAO truckDAO;
  private final TruckWriter writer;
  private final TruckStopDAO stopDAO;
  private final TruckStopWriter truckStopWriter;
  private final Clock clock;

  @Inject
  public DataExportServiceImpl(StorageService storageService, TruckDAO truckDAO, TruckWriter writer, TruckStopDAO truckStopDAO, TruckStopWriter truckStopWriter,
      Clock clock) {
    this.storageService = storageService;
    this.truckDAO = truckDAO;
    this.writer = writer;
    this.stopDAO = truckStopDAO;
    this.truckStopWriter = truckStopWriter;
    this.clock = clock;
  }

  @Override
  public void exportTrucks() throws IOException {
    storageService.writeBuffer(toJsonLine(writer, truckDAO.findAll().stream()),
        "chiftf_datalake", "trucks.json", MediaType.APPLICATION_JSON);

  }

  @Override
  public void exportStopsForMonth(int year, int month) throws IOException {
    String paddedMonth = String.valueOf(month);
    if (paddedMonth.length() == 1) {
      paddedMonth = "0" + paddedMonth;
    }
    DateTime start = new DateTime(year, month, 1, 0, 0, 0, clock.zone());
    DateTime end = start.plusMonths(1);
    Interval range = new Interval(start, end);
    storageService.writeBuffer(toJsonLine(truckStopWriter, stopDAO.findOverRange(null, range).stream()),
        "chiftf_datalake", "stops-" + year + paddedMonth + ".json",
        MediaType.APPLICATION_JSON);
  }

  private static <T> byte[] toJsonLine(JSONWriter<T> writer, Stream<T> items) throws UnsupportedEncodingException {
    return items.map(writer::asString).collect(Collectors.joining("\n")).getBytes("UTF-8");
  }
}
