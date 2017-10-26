package foodtruck.server.job;

import java.io.IOException;

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
import foodtruck.server.CodedServletException;

/**
 * @author aviolette
 * @since 1/13/17
 */
public abstract class AbstractNotificationServlet extends HttpServlet {

  private final TruckStopDAO truckStopDAO;
  private final TrackingDeviceDAO trackingDeviceDAO;
  private final TruckDAO truckDAO;
  private final PublicEventNotificationService notificationService;
  private final StaticConfig config;
  private final EmailSender emailSender;

  AbstractNotificationServlet(TruckStopDAO truckStopDAO, TrackingDeviceDAO trackingDeviceDAO,
      TruckDAO truckDAO, PublicEventNotificationService notificationService,
      EmailSender emailSender, StaticConfig staticConfig) {
    this.truckStopDAO = truckStopDAO;
    this.trackingDeviceDAO = trackingDeviceDAO;
    this.truckDAO = truckDAO;
    this.notificationService = notificationService;
    this.config = staticConfig;
    this.emailSender = emailSender;
  }

  @Override
  protected final void doPost(HttpServletRequest request, HttpServletResponse resp)
      throws ServletException, IOException {
    String stopId = request.getParameter("stopId");
    String deviceId = request.getParameter("deviceId");
    TruckStop stop = truckStopDAO.findByIdOpt(Long.parseLong(stopId))
        .orElseThrow(() -> new CodedServletException(404, "Stop not found " + stopId));
    TrackingDevice device = trackingDeviceDAO.findByIdOpt(Long.parseLong(deviceId))
        .orElseThrow(() -> new CodedServletException(404, "Device not found " + deviceId));
    final String truckId = stop.getTruck()
        .getId();
    Truck truck = truckDAO.findByIdOpt(truckId)
        .orElseThrow(() -> new CodedServletException(404, "Truck not found " + truckId));
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
        "To disable or enable these notifications:\n" + config.getBaseUrl() +
        "/vendor/notifications/" + truckId + "\n";
  }
}
