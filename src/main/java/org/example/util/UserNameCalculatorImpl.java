package org.example.util;

import org.example.Dao.GenericDao;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserNameCalculatorImpl implements UserNameCalculator {

    @Autowired
    private GenericDao<Trainee> traineeDao;

    @Autowired
    private GenericDao<Trainer> trainerDao;

    public String getUserName(String firstName, String lastName) {
        String userName = firstName + "." + lastName;
        int id = calculateUserNumber(userName);
        if (id != 0) {
            userName += id;
        }
        return userName;
    }

    private boolean userNameInDaos(String username) {
        return !traineeDao.findByField("username", username).isEmpty() ||
                !trainerDao.findByField("username", username).isEmpty();
    }

    private int calculateUserNumber(String username) {
        int counter = 0;
        String uniqueName = username;
        while (userNameInDaos(uniqueName)) {
            counter++;
            uniqueName = username + counter;
        }
        return counter;
    }
}
