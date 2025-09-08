package org.example.responseBodies;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.model.Trainee;

import java.time.LocalDate;

@Schema(name = "TraineeDTO", description = "Data transfer object representing a trainee")
public class TraineeDTO {

    @Schema(description = "Trainee's first name")
    private String firstName;

    @Schema(description = "Trainee's last name")
    private String lastName;

    @Schema(description = "Trainee's date of birth", type = "string", format = "date")
    private LocalDate dateOfBirth;

    @Schema(description = "Trainee's address")
    private String address;

    @Schema(description = "Whether the trainee is active")
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
