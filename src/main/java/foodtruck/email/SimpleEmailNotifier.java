package foodtruck.email;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Configuration;
import foodtruck.model.FoodTruckRequest;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TweetSummary;
import foodtruck.util.FriendlyDateOnlyFormat;

/**
 * An email notifier that sends the email immediately.
 * @author aviolette
 * @since 4/29/13
 */
public class SimpleEmailNotifier implements EmailNotifier {
  public static Logger log = Logger.getLogger(SimpleEmailNotifier.class.getName());
  private final ConfigurationDAO configDAO;
  private final MessageBuilder messageBuilder;
  private final DateTimeFormatter dateOnlyFormatter;

  @Inject
  public SimpleEmailNotifier(ConfigurationDAO configurationDAO, MessageBuilder messageBuilder,
      @FriendlyDateOnlyFormat DateTimeFormatter dateOnlyFormat) {
    this.configDAO = configurationDAO;
    this.messageBuilder = messageBuilder;
    this.dateOnlyFormatter = dateOnlyFormat;
  }

  @Override public void systemNotifyOffTheRoad(Truck truck, TweetSummary tweet) {
    String msgBody = MessageFormat.format("This tweet might indicate that {0} is off the road:\n" +
        "\n \"{1}\"\n\n" +
        "Click here to take the truck off the road: " +
        "http://www.chicagofoodtruckfinder.com/admin/trucks/{2}/offtheroad", truck.getName(), tweet.getText(),
        truck.getId());
    sendSystemMessage(truck.getName() + " might be off the road", msgBody);
  }

  @Override public void systemNotifyLocationAdded(Location location, TweetSummary tweet, Truck truck) {
    try {
      sendSystemMessage("New Location Added: " + location.getName(),
          messageBuilder.locationAddedMessage(location, tweet, truck));
    } catch (Exception e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Override public void systemNotifyTrucksAddedByObserver(Map<Truck, TweetSummary> trucksAdded) {
    StringBuilder builder = new StringBuilder("The following trucks were added: \n\n");
    for (Map.Entry<Truck, TweetSummary> entry : trucksAdded.entrySet()) {
      builder.append(entry.getKey().getName()).append(" http://www.chicagofoodtruckfinder.com/admin/trucks/")
          .append(entry.getKey().getId()).append(" => @").append(entry.getValue().getScreenName())
          .append(" '").append(entry.getValue().getText()).append("'\n\n");
    }
    try {
      sendSystemMessage("New stops added by observers", builder.toString());
    } catch (Exception e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Override public void notifyNewFoodTruckRequest(FoodTruckRequest request) {
    try {
      StringBuilder builder = new StringBuilder("The following request was added: \n\n");
      builder.append(request.getEventName()).append("\n\n");
      builder.append(dateOnlyFormatter.print(request.getStartTime())).append(" - ");
      builder.append(dateOnlyFormatter.print(request.getEndTime())).append("\n");
      builder.append("Requested by: ").append(request.getRequester()).append("\n");
      builder.append("Email: ").append(request.getEmail()).append("\n");
      builder.append("Phone: ").append(request.getPhone()).append("\n");
      builder.append("Expected number of guests: ").append(request.getExpectedGuests()).append("\n");
      builder.append("Prepaid: ").append(request.isPrepaid()).append("\n");
      builder.append("\n\n");
      builder.append(request.getDescription());
      sendSystemMessage("New food truck request", builder.toString());
    } catch (Exception e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }

  }

  private void sendSystemMessage(String subject, String msgBody) {
    Configuration config = configDAO.find();
    List<String> receivers = config.getSystemNotificationList();
    String sender = config.getNotificationSender();
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    Message msg = new MimeMessage(session);
    try {
      msg.setFrom(new InternetAddress(sender, "Food Truck Finder"));
      for (String receiver : receivers) {
        msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(receiver));
      }
      msg.setSubject(subject);
      msg.setText(msgBody);
      Transport.send(msg);
    } catch (MessagingException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    } catch (UnsupportedEncodingException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
  }
}
