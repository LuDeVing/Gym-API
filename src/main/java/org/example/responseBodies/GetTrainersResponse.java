package org.example.responseBodies;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Response containing a set of trainers")
public class GetTrainersResponse {

    @Schema(description = "Set of trainers")
    private final Set<TrainerDTO> trainers;

    public GetTrainersResponse(Set<TrainerDTO> trainers) {
        this.trainers = trainers;
    }

    public Set<TrainerDTO> getTrainers() {
        return trainers;
    }
}
