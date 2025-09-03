package org.example.service;


import org.example.Dao.GenericDao;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.model.Training;
import org.example.model.User;
import org.example.util.PasswordGenerator;
import org.example.util.UserNameCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TraineeServiceImpl implements TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private final GenericDao<Trainee> traineeDao;
    private final GenericDao<Trainer> trainerDao;
    private final GenericDao<Training> trainingDao;
    private final UserNameCalculator userNameCalculator;
    private final PasswordGenerator passwordGenerator;

    @Autowired
    public TraineeServiceImpl(GenericDao<Trainee> traineeDao,
                              UserNameCalculator userNameCalculator,
                              GenericDao<Trainer> trainerDao, GenericDao<Training> trainingDao,
                              PasswordGenerator passwordGenerator) {
        this.traineeDao = traineeDao;
        this.userNameCalculator = userNameCalculator;
        this.trainingDao = trainingDao;
        this.passwordGenerator = passwordGenerator;
        this.trainerDao = trainerDao;
    }

    @Override
    public Trainee create(User user, LocalDate date, String address) {

        String userName = userNameCalculator.getUserName(user.getFirstName(), user.getLastName());
        String password = passwordGenerator.generateRandomPassword();

        Trainee trainee = new Trainee(user.getFirstName(), user.getLastName(), userName, password,
                user.isActive(), date, address);

        return traineeDao.create(trainee);

    }

    @Override
    public Optional<Trainee> select(Long Id) {
        return traineeDao.select(Id);
    }

    @Override
    public void update(Trainee trainee) {
        traineeDao.update(trainee);
    }

    @Override
    public void delete(Long Id) {
        traineeDao.delete(Id);
        logger.info("Deleted Trainee with ID: {}", Id);
    }

    @Override
    public Optional<Trainee> selectByUserName(String username) {
        List<Trainee> sol = traineeDao.findByField("username", username);

        if (sol.isEmpty())
            return Optional.empty();

        return Optional.ofNullable(sol.get(0));

    }

    @Override
    public void changePassword(String username, String newPassword) {
        Optional<Trainee> traineeOpt = selectByUserName(username);
        if (traineeOpt.isEmpty()) {
            logger.warn("Cannot change password, trainee with username={} not found", username);
            return;
        }

        Trainee trainee = traineeOpt.get();
        trainee.setPassword(newPassword);
        traineeDao.update(trainee);

        logger.info("Password updated successfully for trainee with username={}", username);
    }

    public void deleteByUserName(String username) {
        Optional<Trainee> traineeOpt = selectByUserName(username);
        if (traineeOpt.isEmpty()) {
            logger.warn("Cannot delete, trainee with username={} not found", username);
            return;
        }

        traineeDao.delete(traineeOpt.get().getUserId());
        logger.info("Trainee deleted successfully: username={}", username);
    }

    public void activate(Long id, boolean activate) {
        Optional<Trainee> traineeOpt = traineeDao.select(id);
        if (traineeOpt.isEmpty()) {
            logger.warn("Cannot activate/deactivate, trainee with id={} not found", id);
            return;
        }

        Trainee trainee = traineeOpt.get();
        trainee.setActive(activate);
        traineeDao.update(trainee);

        logger.info("Trainee with id={} set active={}", id, activate);
    }


    public List<Training> getTrainings(String username, String trainerName, LocalDate from, LocalDate to) {
        Optional<Trainee> traineeOpt = selectByUserName(username);
        if (traineeOpt.isEmpty()) {
            logger.warn("No trainings found, trainee with username={} not found", username);
            return List.of();
        }

        Trainee trainee = traineeOpt.get();

        List<Training> returnTrainings = new ArrayList<>();

        for(Training training: trainee.getTrainings()){


            Optional<Trainer> trainerOp = trainerDao.select(training.getTrainer().getUserId());
            assert(trainerOp.isPresent());
            Trainer trainer = trainerOp.get();

            if(!Objects.equals(trainer.getUsername(), trainerName))
                continue;

            if(!(isWithinRange(training.getTrainingDate(), from, to)))
                continue;

            returnTrainings.add(training);

        }

        return returnTrainings;

    }

    @Override
    public List<Trainer> getUnsignedTrainers(String traineeUsername) {

        Optional<Trainee> traineeOpt = selectByUserName(traineeUsername);
        if (traineeOpt.isEmpty()) {
            logger.warn("Trainee with username={} not found", traineeUsername);
            return List.of();
        }

        Trainee trainee = traineeOpt.get();

        List<Long> assignedTrainerIds = trainee.getTrainings().stream()
                .map(training -> training.getTrainer().getUserId())
                .toList();

        String query;
        if (assignedTrainerIds.isEmpty()) {
            query = "SELECT * FROM trainers";
        } else {
            String ids = assignedTrainerIds.stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            query = "SELECT * FROM trainers WHERE user_id NOT IN (" + ids + ")";
        }

        List<Trainer> unsignedTrainers = trainerDao.runQuery(query);

        logger.info("Found {} unassigned trainers for trainee={}", unsignedTrainers.size(), traineeUsername);
        return unsignedTrainers;
    }


    private boolean isWithinRange(LocalDate date, LocalDate from, LocalDate to) {
        return !(date.isBefore(from) || date.isAfter(to));
    }

}
