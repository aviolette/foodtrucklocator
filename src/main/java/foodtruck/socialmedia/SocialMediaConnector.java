package foodtruck.socialmedia;

import java.util.List;

import foodtruck.model.Story;

/**
 * @author aviolette
 * @since 8/26/15
 */
public interface SocialMediaConnector {
  /**
   * Returns a list of recent stories from the connected service
   */
  List<Story> recentStories();
}
