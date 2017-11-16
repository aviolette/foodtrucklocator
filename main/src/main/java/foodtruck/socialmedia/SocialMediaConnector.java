package foodtruck.socialmedia;

import java.util.List;

import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.model.TwitterNotificationAccount;
import foodtruck.util.ServiceException;

/**
 * @author aviolette
 * @since 8/26/15
 */
public interface SocialMediaConnector {
  /**
   * Returns a list of recent stories from the connected service
   */
  List<Story> recentStories();

  @Deprecated // Let's just use this sendStatusFor
  void updateStatusFor(ScheduleMessage message, Truck truck) throws ServiceException;

  /**
   * Sends a status message to the connected service
   * @param message the message
   * @param truck a truck with a set of social media credentials
   * @param messageSplitter used to break apart messages for space-limited statuses
   */
  void sendStatusFor(String message, Truck truck, MessageSplitter messageSplitter) throws ServiceException;

  void sendStatusFor(String message, TwitterNotificationAccount account,
      MessageSplitter noSplitSplitter) throws ServiceException;
}
