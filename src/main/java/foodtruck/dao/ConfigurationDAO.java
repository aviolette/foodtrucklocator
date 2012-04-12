// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.dao;

import foodtruck.model.Configuration;

/**
 * @author aviolette@gmail.com
 * @since 4/10/12
 */
public interface ConfigurationDAO {
  /**
   * Returns the configuration object
   */
  Configuration findSingleton();

  /**
   * Saves the configuration
   */
  void save(Configuration config);
}
