package org.example.responseBodies;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Response containing a list of trainings for a trainee")
public class TraineeTrainingsResponse {

    @Schema(description = "List of trainings")
    private final List<TrainingDTO> trainings;

    public TraineeTrainingsResponse(List<TrainingDTO> trainings) {
        this.trainings = trainings;
    }

    public List<TrainingDTO> getTrainings() {
        return trainings;
    }
}
