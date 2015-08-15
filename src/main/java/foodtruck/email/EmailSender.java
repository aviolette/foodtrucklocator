package foodtruck.email;

import javax.annotation.Nullable;

/**
 * @author aviolette
 * @since 8/13/15
 */
public interface EmailSender {
  /**
   * Sends a system message (to the configured list)
   * @param subject
   * @param msgBody
   */
  void sendSystemMessage(String subject, String msgBody);

  /**
   * Sends a regular email message
   * @param subject
   * @param receivers
   * @param msgBody
   * @param bccs
   * @param replyTo
   * @return
   */
  boolean sendMessage(String subject, Iterable<String> receivers, String msgBody, Iterable<String> bccs,
      @Nullable String replyTo);
}
