package org.example.responseBodies;

import org.example.model.Trainee;
import org.example.model.Trainer;

import java.time.LocalDate;
import java.util.List;

public class TraineeDTO {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private boolean active;

    public TraineeDTO(Trainee trainee) {
        this.firstName = trainee.getFirstName();
        this.lastName = trainee.getLastName();
        this.dateOfBirth = trainee.getDateOfBirth();
        this.address = trainee.getAddress();
        this.active = trainee.isActive();
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public boolean isActive() { return active; }
}
