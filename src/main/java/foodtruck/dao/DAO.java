package foodtruck.dao;

import java.util.Collection;

import javax.annotation.Nullable;

import foodtruck.model.ModelEntity;

/**
 * @author aviolette@gmail.com
 * @since 5/11/12
 */
public interface DAO<K, T extends ModelEntity> {

  Collection<T> findAll();

  long save(T obj);

  @Nullable T findById(K id);
}
