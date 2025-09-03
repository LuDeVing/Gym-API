package org.example.util;

import org.example.Dao.GenericDao;
import org.example.model.User;

public interface UserNameCalculator {
    String getUserName(String firstName, String lastName);
}
