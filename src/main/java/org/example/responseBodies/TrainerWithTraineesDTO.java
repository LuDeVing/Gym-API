package org.example.responseBodies;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "DTO representing a trainer and their assigned trainees")
public class TrainerWithTraineesDTO {

    @Schema(description = "Trainer information")
    private final TrainerDTO trainer;

    @Schema(description = "Set of trainees assigned to the trainer")
    private final Set<TraineeDTO> trainees;

    public TrainerWithTraineesDTO(TrainerDTO trainer, Set<TraineeDTO> trainees) {
        this.trainer = trainer;
        this.trainees = trainees;
    }

    public Set<TraineeDTO> getTrainees() {
        return trainees;
    }

    public TrainerDTO getTrainer() {
        return trainer;
    }
}
