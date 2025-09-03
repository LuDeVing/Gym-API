package org.example.facade;

import org.example.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GymFacade {

    Trainee createTrainee(User user, LocalDate dateOfBirth, String address);
    Optional<Trainee> selectTrainee(Long id);
    void updateTrainee(Trainee trainee);
    void deleteTrainee(Long id);

    Optional<Trainee> selectByTraineeName(String username);
    void changeTraineePassword(String password, String newPassword);
    void deleteByTraineeUserName(String username);
    void activateTrainee(Long id, boolean activate);

    List<Training> getTraineeTrainings(String username, String TrainerName, LocalDate from, LocalDate to);
    List<Trainer> getUnsignedTrainers(String traineeUserName);

    Trainer createTrainer(User user, String specialization);
    Optional<Trainer> selectTrainer(Long id);
    void updateTrainer(Trainer trainer);

    Optional<Trainer> selectTrainerByUserName(String username);
    void changeTrainerPassword(String password, String newPassword);
    void deleteTrainerByUserName(String username);
    void activateTrainer(Long id, boolean activate);

    List<Training> getTrainerTrainings(String username, String TraineeName, LocalDate from, LocalDate to);

    void createTraining(Training training);
    Optional<Training> selectTraining(Long traineeId, Long trainerId, LocalDate date, TrainingType trainingType);

    TrainingType createTrainingType(TrainingType trainingType);
    Optional<TrainingType> selectTrainingType(String name);
    List<TrainingType> getAllTrainingTypes();

}
