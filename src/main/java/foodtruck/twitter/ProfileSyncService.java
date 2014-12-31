package foodtruck.twitter;

import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 12/30/14
 */
public interface ProfileSyncService {
  /**
   * Creates a new truck profile based on the twitter handle defined in the truck parameter.  The newly created profile
   * is saved and returned. If there is no matching twitter profile, the input truck profile is just saved.  The
   * icon image in the twitter profile will be copied out to google cloud storage.
   * @param truck the truck parameter
   * @return
   */
  Truck createFromTwitter(Truck truck);
}
