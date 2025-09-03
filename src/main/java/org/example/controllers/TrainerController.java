package org.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.facade.GymFacade;
import org.example.model.Trainer;
import org.example.model.Training;
import org.example.model.User;
import org.example.responseBodies.TrainerDTO;
import org.example.responseBodies.TraineeDTO;
import org.example.responseBodies.TrainingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "trainer", produces = {"application/JSON"})
@Tag(name = "Trainer API", description = "Operations for creating, updating, retrieving and deleting trainers in application")
public class TrainerController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerController.class);

    @Autowired
    private GymFacade gymFacade;

    @PostMapping("/create")
    @Operation(
            summary = "Add a new trainer, you don't need to be logged in as one",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully created new trainer profile",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    )
            }
    )
    public ResponseEntity<Map<String, String>> createTrainer(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String specialization
    ) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(true);

        Trainer trainer = gymFacade.createTrainer(user, specialization);

        Map<String, String> result = Map.of(
                "username", trainer.getUsername(),
                "password", trainer.getPassword()
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/get")
    @Operation(
            summary = "Get trainer info, if you are logged in as trainer",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully returned profile information",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User is not authorized to get this trainer info",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Trainer not found",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> getTrainer(
            @RequestParam String username,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        logger.info("GET /trainer/{} called", username);

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("You are not logged in as user: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Trainer> trainer = gymFacade.selectTrainerByUserName(username);

        if (trainer.isEmpty()) {
            logger.warn("Trainer {} not found, returning 404", username);
            return ResponseEntity.notFound().build();
        }

        logger.info("Returning 200 with trainer {}", trainer.get().getUsername());
        return ResponseEntity.ok(
                Map.of(
                        "Trainer", new TrainerDTO(trainer.get()),
                        "Trainees",  trainer.get().getTrainings().stream()
                                .map(Training::getTrainee)
                                .distinct()
                                .map(TraineeDTO::new)
                                .collect(Collectors.toSet())
                )
        );
    }

    @PutMapping("/update")
    @Operation(
            summary = "Update trainer info if logged in as that trainer",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated trainer information",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainerDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User is not authorized to update this trainer",
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
            @RequestParam String username,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam boolean isActive,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        logger.info("PUT /trainer/{} called", username);

        Optional<Trainer> trainer = gymFacade.selectTrainerByUserName(username);

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("You are not logged in as user: {}, cannot update", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (trainer.isEmpty()) {
            logger.warn("Your username \"{}\" is not in database", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        trainer.get().setFirstName(firstName);
        trainer.get().setLastName(lastName);
        trainer.get().setActive(isActive);

        gymFacade.updateTrainer(trainer.get());

        return ResponseEntity.ok(
                Map.of("Trainer", new TrainerDTO(trainer.get()))
        );
    }

    @PatchMapping("/activate")
    @Operation(
            summary = "Activate or deactivate trainer (only for self)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Trainer active status updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
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
    public ResponseEntity<?> activateTrainer(
            @RequestParam String username,
            @RequestParam boolean isActive,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        logger.info("PATCH /trainer/activate called for {}", username);

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("User {} tried to change active status for {}", user.getUsername(), username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only change your own active status"));
        }

        Optional<Trainer> trainer = gymFacade.selectTrainerByUserName(username);

        if (trainer.isEmpty()) {
            logger.warn("Trainer {} not found", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Trainer not found"));
        }

        trainer.get().setActive(isActive);
        gymFacade.updateTrainer(trainer.get());

        logger.info("Trainer {} active status updated to {}", username, isActive);
        return ResponseEntity.ok(Map.of("message", "Trainer active status updated successfully"));
    }

    @GetMapping("/trainings")
    @Operation(
            summary = "Get trainer trainings with optional filters (only self)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returns a list of trainings",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized access",
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
            @RequestParam String username,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String traineeName,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        if (!username.equals(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only view your own trainings"));
        }

        var trainings = gymFacade.getTrainerTrainings(username, traineeName, periodFrom, periodTo)
                .stream()
                .map(TrainingDTO::new)
                .toList();

        return ResponseEntity.ok(trainings);
    }
}
