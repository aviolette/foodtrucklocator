package foodtruck.mail;

import javax.annotation.Nullable;

/**
 * @author aviolette
 * @since 8/13/15
 */
public interface EmailSender {
  /**
   * Sends a system message (to the configured list)
   */
  void sendSystemMessage(String subject, String msgBody);

  /**
   * Sends a regular email message
   */
  boolean sendMessage(String subject, Iterable<String> receivers, String msgBody, Iterable<String> bccs,
      @Nullable String replyTo);
}
