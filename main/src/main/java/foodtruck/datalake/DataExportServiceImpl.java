package foodtruck.datalake;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.server.resources.json.TruckWriter;
import foodtruck.storage.StorageService;

/**
 * @author aviolette
 * @since 2018-12-29
 */
public class DataExportServiceImpl implements DataExportService {

  private static final Logger log = Logger.getLogger(DataExportServiceImpl.class.getName());

  private final StorageService storageService;
  private final TruckDAO truckDAO;
  private final TruckWriter writer;

  @Inject
  public DataExportServiceImpl(StorageService storageService, TruckDAO truckDAO, TruckWriter writer) {
    this.storageService = storageService;
    this.truckDAO = truckDAO;
    this.writer = writer;
  }

  @Override
  public void exportTrucks(String bucket) throws IOException {
    StringBuilder builder = new StringBuilder();
    for (Truck truck : truckDAO.findAll()) {
      try {
        builder.append(writer.asJSON(truck)).append("\n");
      } catch (JSONException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
    storageService.writeBuffer(builder.toString().getBytes(), "chiftf_datalake", "trucks.json",
        MediaType.APPLICATION_JSON);
  }
}
