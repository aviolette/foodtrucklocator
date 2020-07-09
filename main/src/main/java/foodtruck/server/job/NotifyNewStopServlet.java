package foodtruck.server.job;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.annotations.BaseUrl;
import foodtruck.dao.TrackingDeviceDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.dao.TruckStopDAO;
import foodtruck.mail.EmailSender;
import foodtruck.model.TrackingDevice;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.notifications.PublicEventNotificationService;
import foodtruck.time.FriendlyDateTimeFormat;

/**
 * Job that handles sending out notifications when a new stop has been created.
 *
 * @author aviolette
 * @since 11/18/16
 */
@Singleton
public class NotifyNewStopServlet extends AbstractNotificationServlet {

  private final DateTimeFormatter formatter;

  @Inject
  public NotifyNewStopServlet(PublicEventNotificationService notificationService, EmailSender emailSender,
      TruckStopDAO truckStopDAO, TrackingDeviceDAO trackingDeviceDAO, TruckDAO truckDAO,
      @FriendlyDateTimeFormat DateTimeFormatter formatter, @BaseUrl String baseUrl) {
    super(truckStopDAO, trackingDeviceDAO, truckDAO, notificationService, emailSender, baseUrl);
    this.formatter = formatter;
  }

  @Override
  protected void privateNotify(TruckStop stop, TrackingDevice device, Truck truck) {
    //noinspection ConstantConditions
    if (truck.isNotifyOfLocationChanges() && !Strings.isNullOrEmpty(truck.getEmail())) {
      String subject = device.getLabel() + " has parked at " + stop.getLocation()
          .getShortenedName();
      String msgBody =
          "Truck stats:\n\nFuel:           " + device.getFuelLevel() + "\nDevice id: " + device.getDeviceNumber() +
              "\nBattery charge      " + device.getBatteryCharge() + " V\nLast broadcast: " +
              formatter.print(device.getLastBroadcast()) + "\n\n\n" +
              urls(String.valueOf(device.getKey()), truck.getId());
      getEmailSender().sendMessage(subject, ImmutableList.of(truck.getEmail()), msgBody, ImmutableList.of(), null);
    }
  }

  @Override
  protected void publicNotify(TruckStop stop) {
    getNotificationService().notifyStopStart(stop);
  }
}
