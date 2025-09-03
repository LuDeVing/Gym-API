package org.example.service;

import org.example.model.Trainer;
import org.example.model.Training;

import java.util.Optional;

public interface TrainingService {
    Training create(Training training);

    Optional<Training> select(Long trainingId);

}