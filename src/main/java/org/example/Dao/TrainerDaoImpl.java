package org.example.Dao;

import org.example.model.Trainer;
import org.example.storage.StorageSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public class TrainerDaoImpl implements GenericDao<Trainer> {

    private static final Logger logger = LoggerFactory.getLogger(TrainerDaoImpl.class);

    private final StorageSystem<Trainer> trainers;

    @Autowired
    public TrainerDaoImpl(StorageSystem<Trainer> trainers) {
        this.trainers = trainers;
    }

    @Override
    public Trainer create(Trainer trainer) {
        logger.info("Trainer created successfully: {}", trainer);
        return trainers.put(trainer);
    }

    @Override
    public List<Trainer> findByField(String field, String value) {
        return trainers.findByField(field, value);
    }

    @Override
    public Optional<Trainer> select(Long id) {
        Optional<Trainer> trainer = trainers.findById(id);
        if (trainer.isPresent()) {
            logger.info("Trainer selected: {}", trainer.get());
        } else {
            logger.warn("Trainer with id={} not found", id);
        }
        return trainer;
    }

    @Override
    public void update(Trainer trainer) {
        if (trainer.getUserId() == null || trainers.findById(trainer.getUserId()).isEmpty()) {
            logger.warn("Cannot update, trainer does not exist: {}", trainer);
            return;
        }

        trainers.update(trainer);
        logger.info("Trainer updated successfully: {}", trainer);
    }


    @Override
    public List<Trainer> runQuery(String query) {
        return trainers.runQuery(query);
    }

    @Override
    public void delete(Long id) {
        if (trainers.findById(id).isEmpty()) {
            logger.warn("Cannot delete, trainer with id={} not found", id);
            return;
        }

        trainers.delete(id);
        logger.info("Trainer deleted successfully: id={}", id);
    }

}