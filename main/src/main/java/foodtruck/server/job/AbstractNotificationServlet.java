package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.mail.EmailSender;
import foodtruck.model.StaticConfig;
import foodtruck.model.TrackingDevice;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.notifications.PublicEventNotificationService;

/**
 * @author aviolette
 * @since 1/13/17
 */
public abstract class AbstractNotificationServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(AbstractNotificationServlet.class.getName());

  private final TruckStopDAO truckStopDAO;
  private final TrackingDeviceDAO trackingDeviceDAO;
  private final TruckDAO truckDAO;
  private final PublicEventNotificationService notificationService;
  private final StaticConfig config;
  private final EmailSender emailSender;

  AbstractNotificationServlet(TruckStopDAO truckStopDAO, TrackingDeviceDAO trackingDeviceDAO, TruckDAO truckDAO,
      PublicEventNotificationService notificationService, EmailSender emailSender, StaticConfig staticConfig) {
    this.truckStopDAO = truckStopDAO;
    this.trackingDeviceDAO = trackingDeviceDAO;
    this.truckDAO = truckDAO;
    this.notificationService = notificationService;
    this.config = staticConfig;
    this.emailSender = emailSender;
  }

  @Override
  protected final void doPost(HttpServletRequest request,
      HttpServletResponse resp) throws ServletException, IOException {
    String stopId = request.getParameter("stopId");
    String deviceId = request.getParameter("deviceId");
    TruckStop stop = truckStopDAO.findById(Long.parseLong(stopId));
    TrackingDevice device = trackingDeviceDAO.findById(Long.parseLong(deviceId));

    if (stop == null) {
      // just a warning since it's possible and legitimate to delete a stop before this actually gets invoked.
      log.log(Level.WARNING, "Stop not found {0}", stopId);
      return;
    }

    if (device == null) {
      log.log(Level.SEVERE, "Device not found {0}", deviceId);
      return;
    }

    Truck truck = truckDAO.findById(stop.getTruck()
        .getId());
    stop = TruckStop.builder(stop)
        .truck(truck)
        .build();

    publicNotify(stop);

    privateNotify(stop, device, truck);
  }

  protected StaticConfig getConfig() {
    return config;
  }

  EmailSender getEmailSender() {
    return emailSender;
  }

  protected abstract void privateNotify(TruckStop stop, TrackingDevice device, Truck truck);

  protected abstract void publicNotify(TruckStop stop);

  PublicEventNotificationService getNotificationService() {
    return notificationService;
  }

  String urls(String key, String truckId) {
    return "Beacon Information:\n" + config.getBaseUrl() + "/vendor/beacons/" + key + "\n\n" +
        "To disable or enable these notifications:\n" + config.getBaseUrl() + "/vendor/notifications/" + truckId + "\n";
  }
}
