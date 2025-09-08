package org.example.responseBodies;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ExceptionResponse", description = "Standard structure for API error responses")
public class ExceptionResponse {

    @Schema(description = "Detailed error message")
    private String error;

    public ExceptionResponse() {}

    public ExceptionResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
