package foodtruck.email;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.FoodTruckRequest;
import foodtruck.model.Location;
import foodtruck.model.PetitionSignature;
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

  private StringBuilder buildRequest(FoodTruckRequest request, StringBuilder builder) {
    builder.append(request.getEventName()).append("\n\n");
    builder.append("Requested by: ").append(request.getRequester()).append("\n");
    builder.append("Email: ").append(request.getEmail()).append("\n");
    builder.append("Phone: ").append(request.getPhone()).append("\n");
    builder.append("Expected number of guests: ").append(request.getExpectedGuests()).append("\n");
    if (request.isPrepaid()) {
      builder.append("Food is not prepaid\n");
    } else {
      builder.append("Food will be paid for in advance\n");
    }
    builder.append("\n\n");
    builder.append(request.getDescription());
    return builder;
  }

  @Override public boolean notifyFoodTrucksOfRequest(Iterable<String> addresses, FoodTruckRequest request) {
    if (Iterables.size(addresses) == 0) {
      log.log(Level.INFO, "Message for request: {0} not sent 'cause there are no recipients", request.getKey());
      return false;
    }
    log.log(Level.INFO, "Sending Request {0} to {1}", new Object[]{request.getKey(), Joiner.on(",").join(addresses)});
    return sender.sendMessage("Food Trucks Needed: " + request.getEventName(),
        ImmutableSet.of(staticConfig.getNotificationSender()),
        buildRequest(request, new StringBuilder()).toString(), addresses, request.getEmail());
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

  @Override public void notifyVerifyPetitionSignature(PetitionSignature sig) {
    String msgBody = MessageFormat.format("Thank you for signing our petition to bring back the food truck stands at" +
        " 600 W. Chicago.  Please click on this URL to verify your email http://www.chicagofoodtruckfinder.com/petitions/600w/verify/{0}", sig.getSignature());
    log.log(Level.FINE, "Petition signature {0}", sig);
    sender.sendMessage("Petition Signature Needs Verification", ImmutableList.of(sig.getEmail()), msgBody,
        ImmutableList.<String>of(), "no-reply@chicagofoodtruckfinder.com");
  }

  @Override
  public void notifyThanksForSigningPetition(PetitionSignature sig) {
    String msgBody = MessageFormat.format("Thank you for signing our petition to bring back the food truck stands at" +
        " 600 W. Chicago.", sig.getSignature());
    sender.sendMessage("Thank you very much!", ImmutableList.of(sig.getEmail()), msgBody, ImmutableList.<String>of(),
        "no-reply@chicagofoodtruckfinder.com");
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
    String msgBody = MessageFormat.format("This tweet \"{0}\"\n\n might have indicated that there additional trucks " +
        "to be added to the system.\n\n  Click here {1} to add the trucks", text, url);
    sender.sendSystemMessage("Truck was mentioned by another truck", msgBody);
  }
}
