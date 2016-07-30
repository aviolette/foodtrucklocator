package foodtruck.server.job;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.linxup.LinxupConnector;
import foodtruck.linxup.Position;
import foodtruck.model.Location;
import foodtruck.model.TrackingDevice;
import foodtruck.truckstops.FoodTruckStopService;


/**
 * Called periodically to query for updates on the beacon.
 *
 * @author aviolette
 * @since 7/21/16
 */
@Singleton
public class TruckMonitorServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(TruckMonitorServlet.class.getName());
  private final FoodTruckStopService stopService;
  private final LinxupConnector connector;
  private final TrackingDeviceDAO trackingDeviceDAO;

  @Inject
  public TruckMonitorServlet(FoodTruckStopService service, LinxupConnector connector,
      TrackingDeviceDAO trackingDeviceDAO) {
    this.stopService = service;
    this.connector = connector;
    this.trackingDeviceDAO = trackingDeviceDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    List<Position> positionList = connector.findPositions();
    synchronize(positionList);
  }

  /**
   * Synchronize what is returned with existing tracking devices in the DB.  If there is new information, update it in
   * DB.
   */
  private void synchronize(List<Position> positions) {
    // wish I had streams here
    Map<String, TrackingDevice> deviceMap = Maps.newHashMap();
    for (TrackingDevice device : trackingDeviceDAO.findAll()) {
      deviceMap.put(device.getDeviceNumber(), device);
    }
    for (Position position : positions) {
      TrackingDevice device = deviceMap.get(position.getDeviceNumber());
      TrackingDevice.Builder builder = TrackingDevice.builder(device);
      builder.deviceNumber(position.getDeviceNumber())
          .lastLocation(Location.builder().lat(position.getLatLng().getLatitude())
              .lng(position.getLatLng().getLongitude())
              .name("UNKNOWN")
              .build())
          .lastBroadcast(position.getDate())
          .label(position.getVehicleLabel());
      trackingDeviceDAO.save(builder.build());
    }
  }
}
