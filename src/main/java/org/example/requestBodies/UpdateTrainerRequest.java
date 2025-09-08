package org.example.requestBodies;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UpdateTrainerRequest", description = "Request body for updating a trainer profile")
public class UpdateTrainerRequest {

    @NotBlank
    @Schema(description = "Trainer's first name")
    private String firstName;

    @NotBlank
    @Schema(description = "Trainer's last name")
    private String lastName;

    @NotNull
    @Schema(description = "Whether the trainer is active")
    private boolean isActive;

    public UpdateTrainerRequest() {}

    public UpdateTrainerRequest(String firstName, String lastName, boolean isActive) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
