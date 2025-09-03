package org.example.responseBodies;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.example.model.Training;
import org.example.model.TrainingType;

import java.time.LocalDate;
import java.util.Objects;

public class TrainingDTO {
    private final String trainingName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate trainingDate;

    private final TrainingType trainingType;
    private final Integer trainingDuration;
    private final String trainerName;
    private final String traineeName;

    public TrainingDTO(Training tr) {

        this.trainingName = tr.getTrainingName();
        this.trainingDate = tr.getTrainingDate();
        this.trainingType = tr.getTrainingType();

        this.trainingDuration = tr.getTrainingDuration();
        this.trainerName = tr.getTrainer().getUsername();
        this.traineeName = tr.getTrainee().getUsername();

    }

    public String getTrainingName() {
        return trainingName;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public String getTraineeName() {
        return traineeName;
    }

}
