// Copyright 2010 BrightTag, Inc. All rights reserved.
package foodtruck.dao;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since 2/26/12
 */
public interface TruckDAO {

  /**
   * Returns all the trucks
   */
  Collection<Truck> findAll();

  @Nullable Truck findById(String id);

  public void save(Truck truck);

  /**
   * Returns the trucks by their twitter handle.  It is possible for more than one truck to be
   * associated with a twitter handle
   * @param screenName screenName
   * @return the list of trucks associated with the twitter handle (or empty if none found)
   */
  Collection<Truck> findByTwitterId(String screenName);

  Collection<Truck> findAllTwitterTrucks();

  Set<Truck> findTrucksWithCalendars();
}
