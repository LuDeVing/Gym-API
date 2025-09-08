package org.example.responseBodies;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.example.model.Training;
import org.example.model.TrainingType;

import java.time.LocalDate;

@Schema(description = "DTO representing a training session")
public class TrainingDTO {

    @Schema(description = "Name of the training session")
    private final String trainingName;

    @Schema(description = "Date of the training session, formatted as yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate trainingDate;

    @Schema(description = "Type of the training")
    private final TrainingType trainingType;

    @Schema(description = "Duration of the training in minutes")
    private final Integer trainingDuration;

    @Schema(description = "Username of the trainer who conducted the training")
    private final String trainerName;

    @Schema(description = "Username of the trainee who attended the training")
    private final String traineeName;

    public TrainingDTO(Training tr) {
        this.trainingName = tr.getTrainingName();
        this.trainingDate = tr.getTrainingDate();
        this.trainingType = tr.getTrainingType();
        this.trainingDuration = tr.getTrainingDuration();
        this.trainerName = tr.getTrainer().getUsername();
        this.traineeName = tr.getTrainee().getUsername();
    }

    public String getTrainingName() { return trainingName; }
    public LocalDate getTrainingDate() { return trainingDate; }
    public TrainingType getTrainingType() { return trainingType; }
    public Integer getTrainingDuration() { return trainingDuration; }
    public String getTrainerName() { return trainerName; }
    public String getTraineeName() { return traineeName; }
}
