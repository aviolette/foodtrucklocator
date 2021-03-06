package foodtruck.dao;

import java.util.List;
import java.util.Optional;

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
  List<T> findAll();

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
  @Deprecated
  @Nullable T findById(K id);

  /** Finds the entity by ID.
   *
   * @param id the ID
   * @return an optional that will contain the entity if it was found
   */
  Optional<T> findByIdOpt(K id);

  /**
   * Deletes the item in the data store.
   * @param id the ID
   */
  void delete(K id);

  long count();
}
