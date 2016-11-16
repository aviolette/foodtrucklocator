package foodtruck.notifications;

import java.io.UnsupportedEncodingException;
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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import foodtruck.model.StaticConfig;

/**
 * @author aviolette
 * @since 8/13/15
 */
class JavaMailEmailSender implements EmailSender {
  private static final Logger log = Logger.getLogger(JavaMailEmailSender.class.getName());
  private final StaticConfig config;

  @Inject
  public JavaMailEmailSender(StaticConfig staticConfig) {
    this.config = staticConfig;
  }

  @Override
  public void sendSystemMessage(String subject, String msgBody) {
    sendMessage(subject, config.getSystemNotificationList(), msgBody, ImmutableList.<String>of(), null);
  }

  @Override
  public boolean sendMessage(String subject, Iterable<String> receivers, String msgBody, Iterable<String> bccs,
      @Nullable String replyTo) {
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
    } catch (MessagingException | UnsupportedEncodingException e) {
      log.log(Level.WARNING, e.getMessage(), e);
      return false;
    }
    return true;
  }
}
