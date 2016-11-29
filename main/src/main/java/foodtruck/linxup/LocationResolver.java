package foodtruck.linxup;

import javax.annotation.Nullable;

import foodtruck.model.LinxupAccount;
import foodtruck.model.Location;
import foodtruck.model.TrackingDevice;

/**
 * @author aviolette
 * @since 11/29/16
 */
interface LocationResolver {
  /**
   * Translate a raw position from the Linxup device into a Location object.  Since there can be variances in queries to
   * the GPS device, we want the location object to represent the last device value in the case where the data varies
   * but the truck has not actually moved.
   * @param position      the CURRENT raw response from GPS device
   * @param device        the LAST device state (i.e. from fie minutes ago)
   * @param linxupAccount the linxup account information
   */
  Location resolve(Position position, @Nullable TrackingDevice device, LinxupAccount linxupAccount);
}
