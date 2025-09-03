package org.example.Dao;

import org.example.model.TrainingType;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDao implements GenericDao<TrainingType> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public TrainingType create(TrainingType entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public List<TrainingType> findByField(String field, String value) {
        String query = "SELECT t FROM TrainingType t WHERE t." + field + " = :value";
        return entityManager.createQuery(query, TrainingType.class)
                .setParameter("value", value)
                .getResultList();
    }

    @Override
    public List<TrainingType> runQuery(String query) {
        TypedQuery<TrainingType> typedQuery = entityManager.createQuery(query, TrainingType.class);
        return typedQuery.getResultList();
    }

    @Override
    public Optional<TrainingType> select(Long id) {
        TrainingType trainingType = entityManager.find(TrainingType.class, id);
        return Optional.ofNullable(trainingType);
    }

    @Override
    public void update(TrainingType entity) {
        entityManager.merge(entity);
    }

    @Override
    public void delete(Long id) {
        TrainingType trainingType = entityManager.find(TrainingType.class, id);
        if (trainingType != null) {
            entityManager.remove(trainingType);
        }
    }
}
