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
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/trainings", produces = {"application/json"})
@Tag(name = "Training API", description = "Operations for adding trainings and retrieving training types")
public class TrainingController {

    private static final Logger logger = LoggerFactory.getLogger(TrainingController.class);

    @Autowired
    private GymFacade gymFacade;

    @PostMapping
    @Operation(
            summary = "Add a new training session (trainer can only add trainings for themselves)",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Training added successfully"),
                    @ApiResponse(responseCode = "403", description = "User is not authorized to add this training"),
                    @ApiResponse(responseCode = "404", description = "Trainee or Trainer not found"),
                    @ApiResponse(responseCode = "500", description = "Failed to create training")
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
        logger.info("POST /trainings called by {}, transactionID={}", trainerUsername, MDC.get("transactionID"));

        if (!trainerUsername.equals(user.getUsername())) {
            logger.warn("User {} tried to add training as {}, transactionID={}", user.getUsername(), trainerUsername, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only add trainings as yourself"));
        }

        Optional<Trainee> te = gymFacade.selectByTraineeName(traineeUsername);
        if (te.isEmpty()) {
            logger.warn("Trainee {} not found, transactionID={}", traineeUsername, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Trainee not found"));
        }

        Optional<Trainer> tr = gymFacade.selectTrainerByUserName(trainerUsername);
        if (tr.isEmpty()) {
            logger.warn("Trainer {} not found, transactionID={}", trainerUsername, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Trainer not found"));
        }

        TrainingType tt;
        Optional<TrainingType> existingType = gymFacade.selectTrainingType(trainingName);
        tt = existingType.orElseGet(() -> gymFacade.createTrainingType(new TrainingType(trainingName)));

        try {
            gymFacade.createTraining(new Training(te.get(), tr.get(), trainingName, tt, trainingDate, duration));
            logger.info("Training '{}' for trainee {} added by trainer {}, transactionID={}", trainingName, traineeUsername, trainerUsername, MDC.get("transactionID"));
        } catch (Exception e) {
            logger.warn("Failed to create training '{}' for trainee {} by trainer {}, transactionID={}", trainingName, traineeUsername, trainerUsername, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to create training"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Training added successfully"));
    }

    @GetMapping("/types")
    @Operation(
            summary = "Get all available training types",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns a list of all training types")
            }
    )
    public ResponseEntity<?> getTrainingTypes() {
        logger.info("GET /trainings/types called, transactionID={}", MDC.get("transactionID"));
        return ResponseEntity.ok(Map.of("Training Types", gymFacade.getAllTrainingTypes()));
    }
}
