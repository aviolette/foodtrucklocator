package foodtruck.dao;

import javax.annotation.Nullable;

import foodtruck.model.TwitterNotificationAccount;

/**
 * @author aviolette
 * @since 12/3/12
 */
public interface TwitterNotificationAccountDAO extends DAO<Long, TwitterNotificationAccount> {
  /**
   * Finds the twitter notification account by name otherwise {@code null} is returned
   * @param name the address
   */
  @Nullable TwitterNotificationAccount findByLocationName(String name);

  @Nullable TwitterNotificationAccount findByTwitterHandle(String twitterHandle);
}
