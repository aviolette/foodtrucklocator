package foodtruck.email;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.common.base.Throwables;
import com.google.inject.Inject;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Configuration;
import foodtruck.model.Truck;
import foodtruck.model.TweetSummary;

/**
 * @author aviolette
 * @since 4/29/13
 */
public class SimpleEmailNotifier implements EmailNotifier {
  public static Logger log = Logger.getLogger(SimpleEmailNotifier.class.getName());
  private final ConfigurationDAO configDAO;

  @Inject
  public SimpleEmailNotifier(ConfigurationDAO configurationDAO) {
    this.configDAO = configurationDAO;
  }

  @Override public void systemNotifyOffTheRoad(Truck truck, TweetSummary tweet) {
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
      msg.setSubject(truck.getName() + " might be off the road");
      String msgBody = MessageFormat.format("This tweet might indicate that {0} is off the road:\n" +
          "\n \"{1}\"\n\n" +
          "Click here to take the truck off the road: " +
          "http://www.chicagofoodtruckfinder.com/admin/trucks/{2}/offtheroad", truck.getName(), tweet.getText(),
          truck.getId());
      msg.setText(msgBody);
      Transport.send(msg);
    } catch (MessagingException e) {
      throw Throwables.propagate(e);
    } catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }
}
