package foodtruck.server.job;

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.annotations.BaseUrl;
import foodtruck.annotations.SystemNotificationList;
import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.mail.EmailSender;
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

  private final List<String> systemNotificationList;

  @Inject
  public NotifyLeavingStopServlet(TruckStopDAO truckStopDAO, TrackingDeviceDAO trackingDeviceDAO, TruckDAO truckDAO,
      PublicEventNotificationService notificationService, EmailSender emailSender, @BaseUrl String baseUrl,
      @SystemNotificationList List<String> systemNotificationList) {
    super(truckStopDAO, trackingDeviceDAO, truckDAO, notificationService, emailSender, baseUrl);
    this.systemNotificationList = systemNotificationList;
  }

  @Override
  protected void privateNotify(TruckStop stop, TrackingDevice device, Truck truck) {
    String subject = device.getLabel() + " has left " + stop.getLocation()
        .getShortenedName();
    String msgBody = urls(String.valueOf(device.getKey()), truck.getId());
    Iterable<String> emails =
        truck.isNotifyOfLocationChanges() && !Strings.isNullOrEmpty(truck.getEmail()) ? ImmutableList.of(
            truck.getEmail()) : systemNotificationList;
    getEmailSender().sendMessage(subject, emails, msgBody, ImmutableList.of(), null);
  }

  @Override
  protected void publicNotify(TruckStop stop) {
    getNotificationService().notifyStopEnd(stop);
  }
}
