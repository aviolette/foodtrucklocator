package foodtruck.dao;

import java.util.Optional;

import foodtruck.model.PartialLocation;

/**
 * @author aviolette
 * @since 10/7/18
 */
public interface ReverseLookupDAO extends DAO<String, PartialLocation> {
  Optional<PartialLocation> findByLatLng(double lat, double lng);
}
