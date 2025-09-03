package org.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.facade.GymFacade;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.model.Training;
import org.example.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "trainee", produces = {"application/JSON"})
@Tag(name = "Training API", description = "Operations for adding trainings and retrieving training types")
public class TrainingController {

    private static final Logger logger = LoggerFactory.getLogger(TrainingController.class);

    @Autowired
    private GymFacade gymFacade;

    @PostMapping("/trainings/add")
    @Operation(
            summary = "Add a new training session (trainer can only add trainings for themselves)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Training added successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User is not authorized to add this training",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Trainee or Trainer not found",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Failed to create training",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> addTraining(
            @RequestParam String traineeUsername,
            @RequestParam String trainerUsername,
            @RequestParam String trainingName,
            @RequestParam LocalDate trainingDate,
            @RequestParam int duration,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        if (!trainerUsername.equals(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only add trainings as yourself"));
        }

        Optional<Trainee> te = gymFacade.selectByTraineeName(traineeUsername);
        if (te.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Trainee not found"));
        }

        Optional<Trainer> tr = gymFacade.selectTrainerByUserName(trainerUsername);
        if (tr.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Trainer not found"));
        }

        TrainingType tt;
        Optional<TrainingType> existingType = gymFacade.selectTrainingType(trainingName);
        tt = existingType.orElseGet(() -> gymFacade.createTrainingType(new TrainingType(trainingName)));

        try {
            gymFacade.createTraining(new Training(te.get(), tr.get(), trainingName, tt, trainingDate, duration));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to create training"));
        }

        return ResponseEntity.ok(Map.of("message", "Training added successfully"));
    }

    @PostMapping("/trainings/getTrainingTypes")
    @Operation(
            summary = "Get all available training types",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returns a list of all training types",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    )
            }
    )
    public ResponseEntity<?> getTrainingTypes() {
        return ResponseEntity.ok(Map.of("Training Types", gymFacade.getAllTrainingTypes()));
    }

}
