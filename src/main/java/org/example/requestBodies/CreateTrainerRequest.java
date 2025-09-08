package org.example.requestBodies;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "CreateTrainerRequest", description = "Request body for creating a new trainer")
public class CreateTrainerRequest {

    @NotBlank
    @Schema(description = "First name of the trainer", example = "Alice")
    private String firstName;

    @NotBlank
    @Schema(description = "Last name of the trainer")
    private String lastName;

    @NotBlank
    @Schema(description = "Trainer's specialization")
    private String specialization;

    public CreateTrainerRequest() {}

    public CreateTrainerRequest(String firstName, String lastName, String specialization) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
