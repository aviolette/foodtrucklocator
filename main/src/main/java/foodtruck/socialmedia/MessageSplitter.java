package foodtruck.socialmedia;

import java.util.List;

/**
 * An interface for breaking down messages into smaller messages
 * @author aviolette
 * @since 11/15/17
 */
public interface MessageSplitter {

  /**
   * Split a message into smaller messages
   * @param message the message
   * @return an ordered list of all the sub-messages
   */
  List<String> split(String message);
}
