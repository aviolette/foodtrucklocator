// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.dao;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * @author aviolette@gmail.com
 * @since 5/11/12
 */
public interface DAO<K, T> {

  Collection<T> findAll();

  long save(T obj);

  @Nullable T findById(K id);
}
