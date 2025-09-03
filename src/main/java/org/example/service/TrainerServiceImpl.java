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

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TrainerServiceImpl implements TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final GenericDao<Trainer> trainerDao;
    private final GenericDao<Trainee> traineeDao;
    private final UserNameCalculator userNameCalculator;
    private final PasswordGenerator passwordGenerator;


    @Autowired
    public TrainerServiceImpl(GenericDao<Trainer> trainerDao,
                              GenericDao<Trainee> traineeDao,
                              UserNameCalculator userNameCalculator,
                              PasswordGenerator passwordGenerator) {
        this.trainerDao = trainerDao;
        this.traineeDao = traineeDao;
        this.userNameCalculator = userNameCalculator;
        this.passwordGenerator = passwordGenerator;
    }


    @Override
    @Transactional
    public Trainer create(User user, String specialization) {

        String userName = userNameCalculator.getUserName(user.getFirstName(), user.getLastName());
        String password = passwordGenerator.generateRandomPassword();

        Trainer trainer = new Trainer(user.getFirstName(), user.getLastName(), userName, password,
                user.isActive(), specialization);

        return trainerDao.create(trainer);

    }

    @Override
    public Optional<Trainer> select(Long id) {
        return trainerDao.select(id);
    }

    @Override
    public void update(Trainer trainer) {
        trainerDao.update(trainer);
    }

    @Override
    public Optional<Trainer> selectByUserName(String username) {
        List<Trainer> sol = trainerDao.findByField("username", username);
        if (sol.isEmpty())
            return Optional.empty();

        return Optional.ofNullable(sol.get(0));
    }

    @Override
    public void changePassword(String username, String newPassword) {
        Optional<Trainer> trainerOpt = selectByUserName(username);
        if (trainerOpt.isEmpty()) {
            logger.warn("Cannot change password, trainer with username={} not found", username);
            return;
        }

        Trainer trainer = trainerOpt.get();
        trainer.setPassword(newPassword);
        trainerDao.update(trainer);

        logger.info("Password updated successfully for trainer with username={}", username);
    }

    @Override
    public void deleteByUserName(String username) {
        Optional<Trainer> trainerOpt = selectByUserName(username);
        if (trainerOpt.isEmpty()) {
            logger.warn("Cannot delete, trainer with username={} not found", username);
            return;
        }

        trainerDao.delete(trainerOpt.get().getUserId());
        logger.info("Trainer deleted successfully: username={}", username);
    }

    @Override
    public void activate(Long id, boolean activate) {
        Optional<Trainer> trainerOpt = trainerDao.select(id);
        if (trainerOpt.isEmpty()) {
            logger.warn("Cannot activate/deactivate, trainer with id={} not found", id);
            return;
        }

        Trainer trainer = trainerOpt.get();
        trainer.setActive(activate);
        trainerDao.update(trainer);

        logger.info("Trainer with id={} set active={}", id, activate);
    }

    @Override
    public List<Training> getTrainings(String username, String traineeName, LocalDate from, LocalDate to) {
        Optional<Trainer> trainerOpt = selectByUserName(username);
        if (trainerOpt.isEmpty()) {
            logger.warn("No trainings found, trainer with username={} not found", username);
            return List.of();
        }

        Trainer trainer = trainerOpt.get();
        List<Training> returnTrainings = new ArrayList<>();

        for (Training training : trainer.getTrainings()) {
            Optional<Trainee> traineeOp = traineeDao.select(training.getTrainee().getUserId());
            if (traineeOp.isEmpty()) continue;

            Trainee trainee = traineeOp.get();

            if (!Objects.equals(trainee.getUsername(), traineeName))
                continue;

            if (!(isWithinRange(training.getTrainingDate(), from, to)))
                continue;

            returnTrainings.add(training);
        }

        return returnTrainings;
    }

    private boolean isWithinRange(LocalDate date, LocalDate from, LocalDate to) {
        return !(date.isBefore(from) || date.isAfter(to));
    }

}
