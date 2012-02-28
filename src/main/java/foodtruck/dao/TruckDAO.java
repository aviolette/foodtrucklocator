// Copyright 2010 BrightTag, Inc. All rights reserved.
package foodtruck.dao;

import java.util.Collection;

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
}
