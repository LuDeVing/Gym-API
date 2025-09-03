package org.example.service;

import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.model.Training;
import org.example.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee create(User user, LocalDate date, String address);
    Optional<Trainee> select(Long Id);
    void update(Trainee trainee);
    void delete(Long Id);

    Optional<Trainee> selectByUserName(String username);
    void changePassword(String password, String newPassword);
    void deleteByUserName(String username);
    void activate(Long id, boolean activate);

    List<Training> getTrainings(String username, String TrainerName, LocalDate from, LocalDate to);
    List<Trainer> getUnsignedTrainers(String userName);

}
