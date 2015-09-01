package foodtruck.email;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Joiner;
import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.server.vendor.LoginMethod;
import foodtruck.util.TimeFormatter;
import foodtruck.util.TimeOnlyFormatter;

/**
 * An email notifier that sends the email immediately.
 * @author aviolette
 * @since 4/29/13
 */
public class SimpleEmailNotifier implements EmailNotifier {
  public static Logger log = Logger.getLogger(SimpleEmailNotifier.class.getName());
  private final DateTimeFormatter timeOnlyFormatter;
  private final StaticConfig staticConfig;
  private final DateTimeFormatter dateTimeFormatter;
  private final EmailSender sender;

  @Inject
  public SimpleEmailNotifier(
      @TimeOnlyFormatter DateTimeFormatter timeFormatter, StaticConfig staticConfig,
      @TimeFormatter DateTimeFormatter dateTimeFormatter, EmailSender sender) {
    this.timeOnlyFormatter = timeFormatter;
    this.staticConfig = staticConfig;
    this.dateTimeFormatter = dateTimeFormatter;
    this.sender = sender;
  }

  @Override public void systemNotifyOffTheRoad(Truck truck, Story tweet) {
    String msgBody = MessageFormat.format("This tweet might indicate that {0} is off the road:\n" +
        "\n \"{1}\"\n\n" +
        "Click here to take the truck off the road: " + staticConfig.getBaseUrl() +
        "/admin/trucks/{2}/offtheroad", truck.getName(), tweet.getText(),
        truck.getId());
    sender.sendSystemMessage(truck.getName() + " might be off the road", msgBody);
  }

  private String locationAddedMessage(Location location, Story tweet, Truck truck) {
    return MessageFormat.format("This tweet \"{0}\" triggered the following location to be added {1}.  Click here to " +
            "view the location {4}/admin/locations/{2} .  " +
            "Also, view the truck here: {4}/admin/trucks/{3}", tweet.getText(),
        location.getName(), String.valueOf(location.getKey()), truck.getId(), staticConfig.getBaseUrl());
  }

  @Override public void systemNotifyLocationAdded(Location location, Story tweet, Truck truck) {
    try {
      sender.sendSystemMessage("New Location Added: " + location.getName(),
          locationAddedMessage(location, tweet, truck));
    } catch (Exception e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Override public void systemNotifyTrucksAddedByObserver(Map<Truck, Story> trucksAdded) {
    StringBuilder builder = new StringBuilder("The following trucks were added: \n\n");
    for (Map.Entry<Truck, Story> entry : trucksAdded.entrySet()) {
      builder.append(entry.getKey().getName()).append(" ").append(staticConfig.getBaseUrl())
          .append("/admin/trucks/")
          .append(entry.getKey().getId()).append(" => @").append(entry.getValue().getScreenName())
          .append(" '").append(entry.getValue().getText()).append("'\n\n");
    }
    try {
      sender.sendSystemMessage("New stops added by observers", builder.toString());
    } catch (Exception e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Override public void systemNotifyAutoCanceled(Truck truck, Story tweet) {
    String msgBody = MessageFormat.format("This tweet might indicate that {0} is off the road:\n" +
        "\n \"{1}\"\n\n" +
        "Because it was flagged as high-confidenced, all remaining stops were cancelled",
        truck.getName(), tweet.getText());
    sender.sendSystemMessage("Stops auto-canceled for " + truck.getName(), msgBody);
  }

  @Override public void systemNotifiyWeirdStopAdded(TruckStop truckStop, String tweetText) {
    sender.sendSystemMessage("Strange afternoon stop added " + truckStop.getTruck().getName(), MessageFormat.format(
            "This tweet added a new stop for {0} after 1:30pm for a lunch truck: \"{1}\"\n\n" + "Stop that was added: {2} from {3} to {4}\n\n Go here to modify: {6}/admin/trucks/{5}\n\n",
            truckStop.getTruck().getName(), tweetText, truckStop.getLocation().getName(),
            timeOnlyFormatter.print(truckStop.getStartTime()), timeOnlyFormatter.print(truckStop.getEndTime()),
            truckStop.getTruck().getId(), staticConfig.getBaseUrl()));
  }

  @Override public void systemNotifyWarnError(String error) {
    sender.sendSystemMessage("Errors detected! (" + staticConfig.getCityState() + ")",
        "Errors detected on the site:\n\n" + error);
 }

  @Override
  public void systemNotifyVendorPortalLogin(String screenName, LoginMethod loginMethod) {
    String msgBody = MessageFormat.format("Vendor {0} logged in to vendor portal via {1}", screenName,
        loginMethod.toString());
    sender.sendSystemMessage("Vendor portal login", msgBody);
  }

  @Override
  public void notifyAddMentionedTrucks(Set<String> truckIds, TruckStop stop, String text) {
    String truckIdString = Joiner.on(",").join(truckIds),
        url = staticConfig.getBaseUrl() + "/admin/event_at/" + stop.getLocation().getKey() +
        "?selected=" + truckIdString + "&startTime=" + dateTimeFormatter.print(stop.getStartTime()) + "&endTime=" +
            dateTimeFormatter.print(stop.getEndTime());
    String msgBody = MessageFormat.format("This tweet \"{0}\"\n\n from {1} might have indicated that there additional trucks " +
        "to be added to the system.\n\n  Click here {2} to add the trucks", text, stop.getTruck().getName(), url);
    sender.sendSystemMessage("Truck was mentioned by another truck", msgBody);
  }
}
