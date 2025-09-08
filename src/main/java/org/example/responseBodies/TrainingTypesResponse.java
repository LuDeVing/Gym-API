package org.example.responseBodies;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response containing a list of available training types")
public class TrainingTypesResponse {

    @Schema(description = "List of training type names")
    private final List<String> trainingTypes;

    public TrainingTypesResponse(List<String> trainingTypes) {
        this.trainingTypes = trainingTypes;
    }

    public List<String> getTrainingTypes() {
        return trainingTypes;
    }
}
