package org.example.requestBodies;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(name = "UpdateTraineeRequest", description = "Request body for updating a trainee profile")
public class UpdateTraineeRequest {

    @NotBlank
    @Schema(description = "Trainee's first name")
    private String firstName;

    @NotBlank
    @Schema(description = "Trainee's last name")
    private String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Trainee's date of birth")
    private LocalDate dateOfBirth;

    @Schema(description = "Trainee's address")
    private String address;

    @NotNull
    @Schema(description = "Whether trainee is active")
    private boolean isActive;

    public UpdateTraineeRequest() {}

    public UpdateTraineeRequest(String firstName, String lastName, LocalDate dateOfBirth, String address, boolean isActive) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.isActive = isActive;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
