package foodtruck.server.job;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.mail.EmailSender;
import foodtruck.model.StaticConfig;
import foodtruck.model.TrackingDevice;

/**
 * @author aviolette
 * @since 1/12/17
 */

@Singleton
public class CheckDeviceIssuesServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(CheckDeviceIssuesServlet.class.getName());
  private final TrackingDeviceDAO dao;
  private final EmailSender emailSender;
  private final TruckDAO truckDAO;
  private final StaticConfig config;

  @Inject
  public CheckDeviceIssuesServlet(TrackingDeviceDAO dao, EmailSender emailSender, TruckDAO truckDAO,
      StaticConfig config) {
    this.dao = dao;
    this.emailSender = emailSender;
    this.truckDAO = truckDAO;
    this.config = config;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    log.log(Level.INFO, "Checking devices");

    Multimap<String, TrackingDevice> problemDevices = ArrayListMultimap.create();

    for (TrackingDevice device : dao.findAll()) {
      if (device.isHasWarning() && !Strings.isNullOrEmpty(device.getTruckOwnerId())) {
        problemDevices.put(device.getTruckOwnerId(), device);
      }
    }

    for (String truckId : problemDevices.keySet()) {
      sendEmail(truckId, problemDevices.get(truckId));
    }
  }

  private void sendEmail(String truckId, Collection<TrackingDevice> trackingDevices) {
    truckDAO.findByIdOpt(truckId).ifPresent(truck -> {
      if (Strings.isNullOrEmpty(truck.getEmail()) || !truck.isNotifyWhenDeviceIssues()) {
        log.log(Level.INFO, "Device issue notifications disabled for {0}", truckId);
        return;
      }
      String subject = "There are issues with your devices";
      StringBuilder msgBuilder = new StringBuilder();
      for (TrackingDevice device : trackingDevices) {
        msgBuilder.append("Device: ")
            .append(device.getLabel())
            .append(" ")
            .append(config.getBaseUrl())
            .append("/vendor/beacons/")
            .append(device.getKey())
            .append("\n")
            .append("Message: ")
            .append(device.getWarning())
            .append("\n\n\n");
      }
      msgBuilder.append("To enable/disable these notifications:\n")
          .append(config.getBaseUrl())
          .append("/vendor/notifications/")
          .append(truckId)
          .append("\n");

      String msg = msgBuilder.toString();
      log.log(Level.INFO, msg);
      emailSender.sendMessage(subject, ImmutableList.of(truck.getEmail()), msgBuilder.toString(),
          config.getSystemNotificationList(), null);
    });
  }
}
