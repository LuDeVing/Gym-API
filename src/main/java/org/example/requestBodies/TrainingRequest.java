package org.example.requestBodies;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(name = "TrainingRequest", description = "Request body for creating a new training session")
public class TrainingRequest {

    @NotBlank
    @Schema(description = "Username of the trainee")
    private String traineeUsername;

    @NotBlank
    @Schema(description = "Username of the trainer")
    private String trainerUsername;

    @NotBlank
    @Schema(description = "Name of the training session")
    private String trainingName;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date of the training session")
    private LocalDate trainingDate;

    @Schema(description = "Duration of the training session in minutes")
    private int duration;

    public TrainingRequest() {}

    public TrainingRequest(String traineeUsername, String trainerUsername, String trainingName,
                           LocalDate trainingDate, int duration) {
        this.traineeUsername = traineeUsername;
        this.trainerUsername = trainerUsername;
        this.trainingName = trainingName;
        this.trainingDate = trainingDate;
        this.duration = duration;
    }

    public String getTraineeUsername() { return traineeUsername; }
    public void setTraineeUsername(String traineeUsername) { this.traineeUsername = traineeUsername; }

    public String getTrainerUsername() { return trainerUsername; }
    public void setTrainerUsername(String trainerUsername) { this.trainerUsername = trainerUsername; }

    public String getTrainingName() { return trainingName; }
    public void setTrainingName(String trainingName) { this.trainingName = trainingName; }

    public LocalDate getTrainingDate() { return trainingDate; }
    public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
}
