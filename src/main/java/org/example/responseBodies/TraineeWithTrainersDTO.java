package org.example.responseBodies;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Response containing trainee information along with their assigned trainers")
public class TraineeWithTrainersDTO {

    @Schema(description = "Trainee information")
    private final TraineeDTO trainee;

    @Schema(description = "Set of trainers assigned to the trainee")
    private final Set<TrainerDTO> trainers;

    public TraineeWithTrainersDTO(TraineeDTO trainee, Set<TrainerDTO> trainers) {
        this.trainee = trainee;
        this.trainers = trainers;
    }

    public TraineeDTO getTrainee() {
        return trainee;
    }

    public Set<TrainerDTO> getTrainers() {
        return trainers;
    }
}
