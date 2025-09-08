package org.example.responseBodies;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.model.Trainer;

@Schema(description = "Data transfer object representing a trainer")
public class TrainerDTO {

    @Schema(description = "Trainer's username")
    private String username;

    @Schema(description = "Trainer's first name")
    private String firstName;

    @Schema(description = "Trainer's last name")
    private String lastName;

    @Schema(description = "Trainer's specialization")
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
