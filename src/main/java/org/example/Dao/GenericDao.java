package org.example.Dao;

import java.util.List;
import java.util.Optional;

public interface GenericDao<T> {

    T create(T entity);
    List<T> findByField(String field, String value);
    List<T> runQuery(String query);

    Optional<T> select(Long id);
    void update(T entity);
    void delete(Long id);
}
