package org.example.Dao;

import org.example.model.Training;
import org.example.storage.StorageSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDaoImpl implements GenericDao<Training> {

    private static final Logger logger = LoggerFactory.getLogger(TrainingDaoImpl.class);

    private final StorageSystem<Training> trainings;

    @Autowired
    public TrainingDaoImpl(StorageSystem<Training> trainings) {
        this.trainings = trainings;
    }

    @Override
    public Training create(Training training) {
        logger.info("Training created successfully: {}", training);
        return trainings.put(training);
    }

    @Override
    public List<Training> findByField(String field, String value) {
        List<Training> results = trainings.findByField(field, value);
        logger.info("findByField: field={}, value={}, found {} trainings", field, value, results.size());
        return results;
    }

    @Override
    public Optional<Training> select(Long id) {
        Optional<Training> training = trainings.findById(id);
        if (training.isPresent()) {
            logger.info("Training selected: {}", training.get());
        } else {
            logger.warn("Training with id={} not found", id);
        }
        return training;
    }

    @Override
    public void update(Training training) {
        if (training.getId() == null || trainings.findById(training.getId()).isEmpty()) {
            logger.warn("Cannot update, training does not exist: {}", training);
            return;
        }

        trainings.update(training);
        logger.info("Training updated successfully: {}", training);
    }


    @Override
    public List<Training> runQuery(String query) {
        return trainings.runQuery(query);
    }

    @Override
    public void delete(Long id) {
        if (trainings.findById(id).isEmpty()) {
            logger.warn("Cannot delete, training with id={} not found", id);
            return;
        }

        trainings.delete(id);
        logger.info("Training deleted successfully: id={}", id);
    }

}
