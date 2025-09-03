package org.example.service;

import org.example.Dao.GenericDao;
import org.example.model.Trainer;
import org.example.model.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {

    private final GenericDao<Training> trainingDao;

    @Autowired
    public TrainingServiceImpl(GenericDao<Training> trainingDao, GenericDao<Trainer> trainerDao) {
        this.trainingDao = trainingDao;
    }

    @Override
    public Training create(Training training) {
        return trainingDao.create(training);
    }

    @Override
    public Optional<Training> select(Long trainingId) {
        return trainingDao.select(trainingId);
    }

}
