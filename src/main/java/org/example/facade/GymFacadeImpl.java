package org.example.facade;

import org.example.Dao.TrainingTypeDao;
import org.example.model.*;
import org.example.service.TraineeService;
import org.example.service.TrainerService;
import org.example.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class GymFacadeImpl implements GymFacade {

    private static final Logger logger = LoggerFactory.getLogger(GymFacadeImpl.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    private final TrainingTypeDao trainingTypeDao;

    @Autowired
    public GymFacadeImpl(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService,
                         TrainingTypeDao trainingTypeDao) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    public Trainee createTrainee(User user, LocalDate dateOfBirth, String address) {
        logger.info("Created Trainee: {} {}", user.getFirstName(), user.getLastName());
        return traineeService.create(user, dateOfBirth, address);
    }

    @Override
    public Optional<Trainee> selectTrainee(Long id) {
        Optional<Trainee> trainee = traineeService.select(id);
        if (trainee.isEmpty()) {
            logger.warn("Trainee with ID {} not found.", id);
        } else {
            logger.info("Selected Trainee with ID: {}", id);
        }
        return trainee;
    }

    @Override
    public void updateTrainee(Trainee trainee) {
        traineeService.update(trainee);
        logger.info("Updated Trainee with ID: {}", trainee.getUserId());
    }

    @Override
    public void deleteTrainee(Long id) {
        traineeService.delete(id);
        logger.info("Deleted Trainee with ID: {}", id);
    }

    @Override
    public Optional<Trainee> selectByTraineeName(String username) {
        return traineeService.selectByUserName(username);
    }

    @Override
    public void changeTraineePassword(String password, String newPassword) {
        traineeService.changePassword(password, newPassword);
    }

    @Override
    public void deleteByTraineeUserName(String username) {
        traineeService.deleteByUserName(username);
    }

    @Override
    public void activateTrainee(Long id, boolean activate) {
        traineeService.activate(id, activate);
    }

    @Override
    public List<Training> getTraineeTrainings(String username, String TrainerName, LocalDate from, LocalDate to) {
        return traineeService.getTrainings(username, TrainerName, from, to);
    }

    @Override
    public List<Trainer> getUnsignedTrainers(String traineeUserName) {
        return traineeService.getUnsignedTrainers(traineeUserName);
    }

    @Override
    public Trainer createTrainer(User user, String specialization) {
        logger.info("Created Trainer: {} {}", user.getFirstName(), user.getLastName());
        return trainerService.create(user, specialization);
    }

    @Override
    public Optional<Trainer> selectTrainer(Long id) {
        Optional<Trainer> trainer = trainerService.select(id);
        if (trainer.isEmpty()) {
            logger.warn("Trainer with ID {} not found.", id);
        } else {
            logger.info("Selected Trainer with ID: {}", id);
        }
        return trainer;
    }

    @Override
    public void updateTrainer(Trainer trainer) {
        trainerService.update(trainer);
        logger.info("Updated Trainer with ID: {}", trainer.getUserId());
    }

    @Override
    public Optional<Trainer> selectTrainerByUserName(String username) {
        return trainerService.selectByUserName(username);
    }

    @Override
    public void changeTrainerPassword(String password, String newPassword) {
        trainerService.changePassword(password, newPassword);
    }

    @Override
    public void deleteTrainerByUserName(String username) {
        trainerService.deleteByUserName(username);
    }

    @Override
    public void activateTrainer(Long id, boolean activate) {
        trainerService.activate(id, activate);
    }

    @Override
    public List<Training> getTrainerTrainings(String username, String TraineeName, LocalDate from, LocalDate to) {
        return trainerService.getTrainings(username, TraineeName, from, to);
    }

    @Override
    public void createTraining(Training training) {
        trainingService.create(training);
        logger.info("Created Training '{}'", training.getTrainingName());
    }

    @Override
    public Optional<Training> selectTraining(Long traineeId, Long trainerId, LocalDate date, TrainingType trainingType) {

        Long id = Training.generateNumericHash(traineeId, trainerId, date);

        Optional<Training> training = trainingService.select(id);
        if (training.isEmpty()) {
            logger.warn("Training with ID {} not found.", id);
        } else {
            logger.info("Selected Training with ID: {}", id);
        }
        return training;
    }

    @Override
    public TrainingType createTrainingType(TrainingType trainingType) {
        return trainingTypeDao.create(trainingType);
    }

    @Override
    public Optional<TrainingType> selectTrainingType(String name) {
        return trainingTypeDao.findByField("trainingTypeName", name)
                .stream()
                .findFirst();
    }

    @Override
    public List<TrainingType> getAllTrainingTypes() {
        return trainingTypeDao.runQuery("SELECT t FROM TrainingType t");
    }


}
