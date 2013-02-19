package foodtruck.facebook;

/**
 * @author aviolette
 * @since 2/18/13
 */
public interface FacebookService {
  /**
   * Syncs the description and facebook Id for trucks with facebook profiles
   */
  public void syncTruckData();
}
