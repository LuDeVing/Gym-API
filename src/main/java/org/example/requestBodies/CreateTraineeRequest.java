package org.example.requestBodies;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(name = "CreateTraineeRequest", description = "Request body for creating a new trainee")
public class CreateTraineeRequest {

    @NotBlank
    @Schema(description = "First name of the trainee")
    private String firstName;

    @NotBlank
    @Schema(description = "Last name of the trainee")
    private String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date of birth of the trainee in yyyy-MM-dd format")
    private LocalDate dateOfBirth;

    @Schema(description = "Address of the trainee")
    private String address;

    public CreateTraineeRequest() {
    }

    public CreateTraineeRequest(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
