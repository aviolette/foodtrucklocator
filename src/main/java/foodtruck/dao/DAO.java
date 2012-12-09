package foodtruck.dao;

import java.util.Collection;

import javax.annotation.Nullable;

import foodtruck.model.ModelEntity;

/**
 * @author aviolette@gmail.com
 * @since 5/11/12
 */
public interface DAO<K, T extends ModelEntity> {
  /**
   * Finds all the entities.
   */
  Collection<T> findAll();

  /**
   * Saves the entity to persistent storage
   * @param obj the entity
   * @return the entities ID
   */
  long save(T obj);

  /**
   * Finds the entity by ID.  Returns {@code null} if it could not be found
   * @param id the ID
   * @return the entity or {@code null} if it is not found
   */
  @Nullable T findById(K id);

  /**
   * Deletes the item in the data store.
   * @param id the ID
   */
  void delete(K id);
}
