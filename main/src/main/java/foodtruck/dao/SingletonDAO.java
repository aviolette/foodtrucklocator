package foodtruck.dao;

import foodtruck.model.ModelEntity;

/**
 * A DAO that implements singleton semantics
 * @author aviolette@gmail.com
 * @since 9/13/12
 */
public interface SingletonDAO<E extends ModelEntity> {
  /**
   * Returns the entity.  Creates one if it does not exist.
   */
  E find();

  /**
   * Saves the entity to persistent storage.
   */
  void save(E entity);
}
