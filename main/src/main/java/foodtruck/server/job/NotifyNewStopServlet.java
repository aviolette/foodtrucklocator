package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.model.StaticConfig;
import foodtruck.model.TrackingDevice;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.notifications.EmailSender;
import foodtruck.notifications.PublicEventNotificationService;
import foodtruck.util.FriendlyDateTimeFormat;

/**
 * Job that handles sending out notifications when a new stop has been created.
 *
 * @author aviolette
 * @since 11/18/16
 */
@Singleton
public class NotifyNewStopServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(NotifyNewStopServlet.class.getName());

  private final PublicEventNotificationService notificationService;
  private final EmailSender emailSender;
  private final TruckStopDAO truckStopDAO;
  private final TrackingDeviceDAO trackingDeviceDAO;
  private final TruckDAO truckDAO;
  private final StaticConfig config;
  private final DateTimeFormatter formatter;

  @Inject
  public NotifyNewStopServlet(PublicEventNotificationService notificationService, EmailSender emailSender,
      TruckStopDAO truckStopDAO, TrackingDeviceDAO trackingDeviceDAO, TruckDAO truckDAO, StaticConfig config,
      @FriendlyDateTimeFormat DateTimeFormatter formatter) {
    this.notificationService = notificationService;
    this.emailSender = emailSender;
    this.truckStopDAO = truckStopDAO;
    this.trackingDeviceDAO = trackingDeviceDAO;
    this.truckDAO = truckDAO;
    this.config = config;
    this.formatter = formatter;
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String stopId = request.getParameter("stopId");
    String deviceId = request.getParameter("deviceId");
    TruckStop stop = truckStopDAO.findById(Long.parseLong(stopId));
    TrackingDevice device = trackingDeviceDAO.findById(Long.parseLong(deviceId));

    if (stop == null) {
      log.log(Level.SEVERE, "Stop not found {0}", stopId);
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

    notificationService.notifyStopStart(stop);

    //noinspection ConstantConditions
    if (truck.isNotifyOfLocationChanges() && !Strings.isNullOrEmpty(truck.getEmail())) {
      String subject = device.getLabel() + " has parked at " + stop.getLocation()
          .getShortenedName();
      String msgBody = "Truck stats:\n\nFuel:           " +
          device.getFuelLevel() + "%\nDevice id: " +
          device.getDeviceNumber() + "\nBattery charge      " +
          device.getBatteryCharge() + " V\nLast broadcast: " +
          formatter.print(device.getLastBroadcast());
      emailSender.sendMessage(subject, ImmutableList.of(truck.getEmail()), msgBody, config.getSystemNotificationList(),
          null);
    }
  }
}
