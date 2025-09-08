package org.example.requestBodies;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for updating a single boolean parameter")
public class UpdateActiveRequest {

    @NotNull
    @Schema(description = "New value for the parameter")
    private Boolean isActive;

    public UpdateActiveRequest() {
    }

    public UpdateActiveRequest(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
