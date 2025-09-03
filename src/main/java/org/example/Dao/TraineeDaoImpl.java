package org.example.Dao;

import org.example.model.Trainee;
import org.example.storage.StorageSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDaoImpl implements GenericDao<Trainee> {

    private static final Logger logger = LoggerFactory.getLogger(TraineeDaoImpl.class);

    private final StorageSystem<Trainee> trainees;

    @Autowired
    public TraineeDaoImpl(StorageSystem<Trainee> storageSystem) {
        this.trainees = storageSystem;
    }

    @Override
    public Trainee create(Trainee trainee) {
        logger.info("Trainee created successfully: {}", trainee);
        return trainees.put(trainee);
    }

    @Override
    public List<Trainee> findByField(String field, String value) {
        return trainees.findByField(field, value);
    }

    @Override
    public List<Trainee> runQuery(String query) {
        return trainees.runQuery(query);
    }

    @Override
    public Optional<Trainee> select(Long id) {
        Optional<Trainee> trainee = trainees.findById(id);
        if (trainee.isPresent()) {
            logger.info("Trainee selected: {}", trainee.get());
        } else {
            logger.warn("Trainee with id={} not found", id);
        }
        return trainee;
    }

    @Override
    public void update(Trainee trainee) {
        if (trainee.getUserId() == null || trainees.findById(trainee.getUserId()).isEmpty()) {
            logger.warn("Cannot update, trainee does not exist: {}", trainee);
            return;
        }

        trainees.update(trainee);
        logger.info("Trainee updated successfully: {}", trainee);
    }

    @Override
    public void delete(Long id) {
        if (trainees.findById(id).isEmpty()) {
            logger.warn("Cannot delete, trainee with id={} not found", id);
            return;
        }

        trainees.delete(id);
        logger.info("Trainee deleted successfully: id={}", id);
    }

}
