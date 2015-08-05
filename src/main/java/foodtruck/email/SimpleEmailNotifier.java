package foodtruck.email;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Configuration;
import foodtruck.model.FoodTruckRequest;
import foodtruck.model.Location;
import foodtruck.model.PetitionSignature;
import foodtruck.model.StaticConfig;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.model.TweetSummary;
import foodtruck.server.vendor.LoginMethod;
import foodtruck.util.TimeOnlyFormatter;

/**
 * An email notifier that sends the email immediately.
 * @author aviolette
 * @since 4/29/13
 */
public class SimpleEmailNotifier implements EmailNotifier {
  public static Logger log = Logger.getLogger(SimpleEmailNotifier.class.getName());
  private final ConfigurationDAO configDAO;
  private final DateTimeFormatter timeOnlyFormatter;
  private final StaticConfig staticConfig;

  @Inject
  public SimpleEmailNotifier(ConfigurationDAO configurationDAO,
      @TimeOnlyFormatter DateTimeFormatter timeFormatter, StaticConfig staticConfig) {
    this.configDAO = configurationDAO;
    this.timeOnlyFormatter = timeFormatter;
    this.staticConfig = staticConfig;
  }

  @Override public void systemNotifyOffTheRoad(Truck truck, TweetSummary tweet) {
    String msgBody = MessageFormat.format("This tweet might indicate that {0} is off the road:\n" +
        "\n \"{1}\"\n\n" +
        "Click here to take the truck off the road: " + staticConfig.getBaseUrl() +
        "/admin/trucks/{2}/offtheroad", truck.getName(), tweet.getText(),
        truck.getId());
    sendSystemMessage(truck.getName() + " might be off the road", msgBody);
  }

  private String locationAddedMessage(Location location, TweetSummary tweet, Truck truck) {
    return MessageFormat.format("This tweet \"{0}\" triggered the following location to be added {1}.  Click here to " +
            "view the location {4}/admin/locations/{2} .  " +
            "Also, view the truck here: {4}/admin/trucks/{3}", tweet.getText(),
        location.getName(), String.valueOf(location.getKey()), truck.getId(), staticConfig.getBaseUrl());
  }

  @Override public void systemNotifyLocationAdded(Location location, TweetSummary tweet, Truck truck) {
    try {
      sendSystemMessage("New Location Added: " + location.getName(),
          locationAddedMessage(location, tweet, truck));
    } catch (Exception e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Override public void systemNotifyTrucksAddedByObserver(Map<Truck, TweetSummary> trucksAdded) {
    StringBuilder builder = new StringBuilder("The following trucks were added: \n\n");
    for (Map.Entry<Truck, TweetSummary> entry : trucksAdded.entrySet()) {
      builder.append(entry.getKey().getName()).append(" ").append(staticConfig.getBaseUrl())
          .append("/admin/trucks/")
          .append(entry.getKey().getId()).append(" => @").append(entry.getValue().getScreenName())
          .append(" '").append(entry.getValue().getText()).append("'\n\n");
    }
    try {
      sendSystemMessage("New stops added by observers", builder.toString());
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
    return sendMessage("Food Trucks Needed: " + request.getEventName(), ImmutableSet
        .of(configDAO.find().getNotificationSender()) ,
        buildRequest(request, new StringBuilder()).toString(), addresses, request.getEmail());
  }

  @Override public void systemNotifyAutoCanceled(Truck truck, TweetSummary tweet) {
    String msgBody = MessageFormat.format("This tweet might indicate that {0} is off the road:\n" +
        "\n \"{1}\"\n\n" +
        "Because it was flagged as high-confidenced, all remaining stops were cancelled",
        truck.getName(), tweet.getText());
    sendSystemMessage("Stops auto-canceled for " + truck.getName(), msgBody);
  }

  @Override public void systemNotifiyWeirdStopAdded(TruckStop truckStop, String tweetText) {
    sendSystemMessage("Strange afternoon stop added " + truckStop.getTruck().getName(),
        MessageFormat.format("This tweet added a new stop for {0} after 1:30pm for a lunch truck: \"{1}\"\n\n" +
          "Stop that was added: {2} from {3} to {4}\n\n Go here to modify: {6}/admin/trucks/{5}\n\n",
          truckStop.getTruck().getName(), tweetText,
          truckStop.getLocation().getName(), timeOnlyFormatter.print(truckStop.getStartTime()),
          timeOnlyFormatter.print(truckStop.getEndTime()), truckStop.getTruck().getId(), staticConfig.getBaseUrl()));
  }

  @Override public void systemNotifyWarnError(String error) {
    sendSystemMessage("Errors detected! (" + staticConfig.getCityState() + ")", "Errors detected on the site:\n\n" + error);
 }

  @Override public void notifyVerifyPetitionSignature(PetitionSignature sig) {
    String msgBody = MessageFormat.format("Thank you for signing our petition to bring back the food truck stands at" +
        " 600 W. Chicago.  Please click on this URL to verify your email http://www.chicagofoodtruckfinder.com/petitions/600w/verify/{0}", sig.getSignature());
    log.log(Level.FINE, "Petition signature {0}", sig);
    sendMessage("Petition Signature Needs Verification", ImmutableList.of(sig.getEmail()),
        msgBody,
        ImmutableList.<String>of(), "no-reply@chicagofoodtruckfinder.com");
  }

  @Override public void notifyThanksForSigningPetition(PetitionSignature sig) {
    String msgBody = MessageFormat.format("Thank you for signing our petition to bring back the food truck stands at" +
        " 600 W. Chicago.", sig.getSignature());
    sendMessage("Thank you very much!", ImmutableList.of(sig.getEmail()), msgBody, ImmutableList.<String>of(),
        "no-reply@chicagofoodtruckfinder.com");
  }

  @Override
  public void systemNotifyVendorPortalLogin(String screenName, LoginMethod loginMethod) {
    String msgBody = MessageFormat.format("Vendor {0} logged in to vendor portal via {1}", screenName,
        loginMethod.toString());
    sendSystemMessage("Vendor portal login", msgBody);
  }

  private boolean sendMessage(String subject, Iterable<String> receivers, String msgBody, Iterable<String> bccs,
      @Nullable String replyTo) {
    Configuration config = configDAO.find();
    if (Iterables.isEmpty(receivers)) {
      log.log(Level.INFO, "No email addresses specified in receiver list for message: {0}", msgBody);
      return false;
    }
    String sender = config.getNotificationSender();
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    Message msg = new MimeMessage(session);
    try {
      msg.setFrom(new InternetAddress(sender, "Food Truck Finder"));
      for (String receiver : receivers) {
        if (Strings.isNullOrEmpty(receiver)) {
          continue;
        }
        msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(receiver));
      }
      for (String receiver : bccs) {
        if (Strings.isNullOrEmpty(receiver)) {
          continue;
        }
        msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(receiver));
      }
      if (!Strings.isNullOrEmpty(replyTo)) {
        msg.setReplyTo(new Address[] { new InternetAddress(replyTo) });
      }
      msg.setSubject(subject);
      msg.setText(msgBody);
      Transport.send(msg);
    } catch (MessagingException e) {
      log.log(Level.WARNING, e.getMessage(), e);
      return false;
    } catch (UnsupportedEncodingException e) {
      log.log(Level.WARNING, e.getMessage(), e);
      return false;
    }
    return true;
  }

  private void sendSystemMessage(String subject, String msgBody) {
    sendMessage(subject, configDAO.find().getSystemNotificationList(), msgBody, ImmutableList.<String>of(), null);
  }
}
