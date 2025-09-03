package org.example.responseBodies;

import org.example.model.Trainer;
import org.example.model.TrainingType;

public class TrainerDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;

    public TrainerDTO(Trainer trainer) {
        this.username = trainer.getUsername();
        this.firstName = trainer.getFirstName();
        this.lastName = trainer.getLastName();
        this.specialization = trainer.getSpecialization();
    }

    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSpecialization() { return specialization; }
}
