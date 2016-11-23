package foodtruck.linxup;

import javax.annotation.Nullable;

import foodtruck.model.TrackingDevice;

/**
 * Service that detects if a tracking device is at a blacklisted location.
 * @author aviolette
 * @since 11/22/16
 */
interface BlacklistedLocationMatcher {
  boolean isBlacklisted(@Nullable TrackingDevice device);
}
