package org.example.storage;

import java.util.List;
import java.util.Optional;

public interface StorageSystem <T> {

    Optional<T> findById(Long id);

    List<T> findByField(String field, String value);
    List<T> runQuery(String query);

    T put(T entity);
    void delete(Long id);

    void update(T entity);

}
