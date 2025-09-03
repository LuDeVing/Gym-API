package org.example.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public class StorageSystemImpl<T> implements StorageSystem<T> {

    @PersistenceContext
    private EntityManager em;

    private final Class<T> type;
    private final Logger logger = LoggerFactory.getLogger(StorageSystemImpl.class.getName());

    public StorageSystemImpl(Class<T> type) {
        this.type = type;
    }

    @Override
    public Optional<T> findById(Long id) {
        return Optional.ofNullable(em.find(type, id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findByField(String field, String value) {
        Table table = type.getAnnotation(Table.class);
        String tableName = (table != null && !table.name().isEmpty())
                ? table.name()
                : type.getSimpleName();

        String query = "select * from " + tableName + " where " + field + " = :value";
        List<T> results = (List<T>) em.createNativeQuery(query, type)
                .setParameter("value", value)
                .getResultList();

        logger.info("{} findByField: field={}, value={}, found={}", type.getSimpleName(), field, value, results.size());
        return results;
    }

    public List<T> runQuery(String query){
        List<T> results = (List<T>) em.createNativeQuery(query, type).getResultList();
        logger.info("ran query");
        return results;
    }

    @Override
    @Transactional
    public T put(T entity) {
        em.clear();
        em.persist(entity);
        logger.info("{} persisted: {}", type.getSimpleName(), entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        T entity = em.find(type, id);
        if (entity != null) {
            em.remove(entity);
            logger.info("{} deleted: id={}", type.getSimpleName(), id);
        } else {
            logger.warn("{} delete failed, entity not found: id={}", type.getSimpleName(), id);
        }
    }

    @Override
    @Transactional
    public void update(T entity) {
        em.merge(entity);
        logger.info("{} updated: {}", type.getSimpleName(), entity);
    }
}
