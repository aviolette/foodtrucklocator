package foodtruck.server.job;

import javax.inject.Singleton;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

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
 * @since 1/12/17
 */
@Singleton
public class NotifyLeavingStopServlet extends AbstractNotificationServlet {

  @Inject
  public NotifyLeavingStopServlet(TruckStopDAO truckStopDAO, TrackingDeviceDAO trackingDeviceDAO, TruckDAO truckDAO,
      PublicEventNotificationService notificationService, EmailSender emailSender, StaticConfig staticConfig) {
    super(truckStopDAO, trackingDeviceDAO, truckDAO, notificationService, emailSender, staticConfig);
  }

  @Override
  protected void privateNotify(TruckStop stop, TrackingDevice device, Truck truck) {
    //noinspection ConstantConditions
    if (truck.isNotifyWhenLeaving() && !Strings.isNullOrEmpty(truck.getEmail())) {
      String subject = device.getLabel() + " has left " + stop.getLocation()
          .getShortenedName();
      String msgBody = getConfig().getBaseUrl() + "/vendor/beacons/" + device.getKey() + "\n";
      getEmailSender().sendMessage(subject, ImmutableList.of(truck.getEmail()), msgBody,
          getConfig().getSystemNotificationList(), null);
    }
  }

  @Override
  protected void publicNotify(TruckStop stop) {
    getNotificationService().notifyStopEnd(stop);
  }
}
