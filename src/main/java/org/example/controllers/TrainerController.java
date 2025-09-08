package org.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.exceptions.ForbiddenOperationException;
import org.example.exceptions.NotFoundException;
import org.example.facade.GymFacade;
import org.example.model.Trainer;
import org.example.model.Training;
import org.example.model.User;
import org.example.requestBodies.CreateTrainerRequest;
import org.example.requestBodies.UpdateActiveRequest;
import org.example.requestBodies.UpdateTrainerRequest;
import org.example.responseBodies.TrainerDTO;
import org.example.responseBodies.TraineeDTO;
import org.example.responseBodies.TrainerWithTraineesDTO;
import org.example.responseBodies.TrainingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/trainers", produces = {"application/json"})
@Tag(name = "Trainer API", description = "Operations for creating, updating, retrieving and deleting trainers in application")
public class TrainerController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerController.class);

    @Autowired
    private GymFacade gymFacade;

    @PostMapping
    @Operation(
            summary = "Add a new trainer, you don't need to be logged in as one",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Successfully created new trainer profile",
                            content = @Content(mediaType = "application/json", schema = @Schema(
                                    type = "object",
                                    additionalPropertiesSchema = String.class
                            )
                            )
                    )
            }
    )
    public ResponseEntity<Map<String, String>> createTrainer(
            @RequestBody CreateTrainerRequest request
    ) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);

        Trainer trainer = gymFacade.createTrainer(user, request.getSpecialization());

        Map<String, String> result = Map.of(
                "username", trainer.getUsername(),
                "password", trainer.getPassword()
        );

        logger.info("new trainer with username: {} created, transactionID={}", trainer.getUsername(), MDC.get("transactionID"));

        return ResponseEntity.status(201).body(result);
    }

    @GetMapping("/{username}")
    @Operation(
            summary = "Get trainer info, if you are logged in as trainer",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully returned profile information",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainerWithTraineesDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User is forbidden to get this trainer info",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Trainer not found",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<TrainerWithTraineesDTO> getTrainer(
            @PathVariable String username,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) throws ForbiddenOperationException, NotFoundException {
        logger.info("GET /trainers/{} called, transactionID={}", username, MDC.get("transactionID"));

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("You are not logged in as user: {}, transactionID={}", username, MDC.get("transactionID"));
            throw new ForbiddenOperationException("You are not logged in as this trainer");
        }

        Optional<Trainer> trainer = gymFacade.selectTrainerByUserName(username);

        if (trainer.isEmpty()) {
            logger.warn("Trainer {} not found, returning 404, transactionID={}", username, MDC.get("transactionID"));
            throw new NotFoundException("Trainer not found");
        }

        TrainerWithTraineesDTO response = new TrainerWithTraineesDTO(
                new TrainerDTO(trainer.get()),
                trainer.get().getTrainings().stream()
                        .map(Training::getTrainee)
                        .distinct()
                        .map(TraineeDTO::new)
                        .collect(Collectors.toSet())
        );

        logger.info("Returning 200 with trainer {}, transactionID={}", trainer.get().getUsername(), MDC.get("transactionID"));
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{username}")
    @Operation(
            summary = "Update trainer info if logged in as that trainer",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated trainer information",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainerDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User is forbidden to update this trainer",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Trainer not found in the database",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> updateTrainer(
            @PathVariable String username,
            @RequestBody UpdateTrainerRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) throws NotFoundException, ForbiddenOperationException {
        logger.info("PUT /trainers/{} called, transactionID={}", username, MDC.get("transactionID"));

        Optional<Trainer> trainer = gymFacade.selectTrainerByUserName(username);

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("You are not logged in as user: {}, cannot update, transactionID={}", username, MDC.get("transactionID"));
            throw new ForbiddenOperationException("You are not logged in as this trainer");
        }

        if (trainer.isEmpty()) {
            logger.warn("Your username \"{}\" is not in database, transactionID={}", username, MDC.get("transactionID"));
            throw new NotFoundException("Trainer not found");
        }

        trainer.get().setFirstName(request.getFirstName());
        trainer.get().setLastName(request.getLastName());
        trainer.get().setActive(request.isActive());

        gymFacade.updateTrainer(trainer.get());

        return ResponseEntity.ok(Map.of("Trainer", new TrainerDTO(trainer.get())));
    }

    @PatchMapping("/{username}/active")
    @Operation(
            summary = "Activate or deactivate trainer (only for self)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Trainer active status updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(type = "object", additionalPropertiesSchema = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to change other trainer's status",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Trainer not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    )
            }
    )
    public ResponseEntity<?> updateActiveStatus(
            @PathVariable String username,
            @RequestBody UpdateActiveRequest activeRequest,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) throws NotFoundException, ForbiddenOperationException {
        logger.info("PATCH /trainers/{}/activate called, transactionID={}", username, MDC.get("transactionID"));

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("User {} tried to change active status for {}, transactionID={}", user.getUsername(), username, MDC.get("transactionID"));
            throw new ForbiddenOperationException("You can only change your own active status");
        }

        Optional<Trainer> trainer = gymFacade.selectTrainerByUserName(username);

        if (trainer.isEmpty()) {
            logger.warn("Trainer {} not found, transactionID={}", username, MDC.get("transactionID"));
            throw new NotFoundException("Trainer not found");
        }

        trainer.get().setActive(activeRequest.getIsActive());
        gymFacade.updateTrainer(trainer.get());

        logger.info("Trainer {} active status updated to {}, transactionID={}", username, activeRequest.getIsActive(), MDC.get("transactionID"));
        return ResponseEntity.ok(Map.of("message", "Trainer active status updated successfully"));
    }

    @GetMapping("/{username}/trainings")
    @Operation(
            summary = "Get trainer trainings with optional filters (only self)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returns a list of trainings",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TrainingDTO.class, type = "array")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden access",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Trainer not found",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String traineeName,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) throws ForbiddenOperationException {
        logger.info("GET /trainers/{}/trainings called, transactionID={}", username, MDC.get("transactionID"));

        if (!username.equals(user.getUsername())) {
            logger.warn("You can only view your own trainings, user={}, transactionID={}", username, MDC.get("transactionID"));
            throw new ForbiddenOperationException("You can only view your own trainings");
        }

        var trainings = gymFacade.getTrainerTrainings(username, traineeName, periodFrom, periodTo)
                .stream()
                .map(TrainingDTO::new)
                .toList();

        return ResponseEntity.ok(trainings);
    }
}
