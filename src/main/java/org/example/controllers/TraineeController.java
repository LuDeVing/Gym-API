package org.example.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.facade.GymFacade;
import org.example.model.Trainee;
import org.example.model.Training;
import org.example.model.User;
import org.example.responseBodies.TraineeDTO;
import org.example.responseBodies.TrainerDTO;
import org.example.responseBodies.TrainingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "trainee", produces = {"application/JSON"})
@Tag(name = "Trainee API", description = "Operations for creating, updating, retrieving and deleting trainees in application")
public class TraineeController {

    private static final Logger logger = LoggerFactory.getLogger(TraineeController.class);

    @Autowired
    private GymFacade gymFacade;

    @PostMapping("/create")
    @Operation(summary = "Add a new trainee, you don't need to be logged in as one",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully created new trainee profile",
                            content = @Content(mediaType = "application/json", schema = @Schema(
                                    type = "object",
                                    additionalPropertiesSchema = String.class
                            )
                            )
                    )

            }
    )
    public ResponseEntity<Map<String, String>> createTrainee(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false) LocalDate dateOfBirth,
            @RequestParam(required = false) String address
    ) {

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(true);
        Trainee trainee = gymFacade.createTrainee(user, dateOfBirth, address);

        Map<String, String> result = new HashMap<>();
        result.put("username", trainee.getUsername());
        result.put("password", trainee.getPassword());

        logger.info("new trainee with username: {} created, transactionID={}", trainee.getUsername(), MDC.get("transactionID"));

        return ResponseEntity.ok(result);
    }

    @GetMapping("/get")
    @Operation(summary = "Get trainee info, if you are logged in as trainee",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully returned profile information",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User is not authorized to get the information",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The requested resource was not found",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> getTrainee(
            @RequestParam String username,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        logger.info("GET /trainee/{} called, transactionID={}", username, MDC.get("transactionID"));

        if (!Objects.equals(username, user.getUsername())){
            logger.warn("You are not logged in as user: {}, transactionID={}", username, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);

        if(trainee.isEmpty()){
            logger.warn("Trainee {} not found, returning 404, transactionID={}", username, MDC.get("transactionID"));
            return ResponseEntity.notFound().build();
        }

        logger.info("Returning 200 with trainee {}, transactionID={}", trainee.get().getUsername(), MDC.get("transactionID"));

        return ResponseEntity.ok(
                Map.of(
                        "Trainee", new TraineeDTO(trainee.get()),
                        "Trainers", trainee.get().getTrainings().stream()
                                .map(Training::getTrainer)
                                .distinct()
                                .map(TrainerDTO::new)
                                .collect(Collectors.toSet())
                )
        );
    }

    @PutMapping("/update")
    @Operation(
            summary = "Update trainee info if logged in as that trainee",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated trainee information",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TraineeDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User is not authorized to update this trainee",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Trainee not found in the database",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> updateTrainee(
            @RequestParam String username,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false) LocalDate dateOfBirth,
            @RequestParam(required = false) String address,
            @RequestParam boolean isActive,

            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ){

        logger.info("PUT /trainee/{} called, transactionID={}", username, MDC.get("transactionID"));

        Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);

        if (!Objects.equals(username, user.getUsername())){
            logger.warn("You are not logged in as user: {}, cannot update, transactionID={}", username, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if(trainee.isEmpty()){
            logger.warn("Your username \"{}\" is not in database, transactionID={}", username, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        trainee.get().setFirstName(firstName);
        trainee.get().setLastName(lastName);
        trainee.get().setActive(isActive);

        if (dateOfBirth != null)
            trainee.get().setDateOfBirth(dateOfBirth);

        if (address != null)
            trainee.get().setAddress(address);

        gymFacade.updateTrainee(trainee.get());

        return ResponseEntity.ok(Map.of("Trainee", new TraineeDTO(trainee.get())));

    }

    @DeleteMapping("/delete")
    @Operation(
            summary = "delete trainee info if logged in as that trainee",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted trainee information",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TraineeDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "User is not authorized to delete this trainee",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Trainee not found in the database",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> deleteTrainee(
            @RequestParam String username,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        logger.info("DELETE /trainee/{} called, transactionID={}", username, MDC.get("transactionID"));

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("User {} tried to delete another trainee, transactionID={}", user.getUsername(), MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "You can only delete your own profile"));
        }

        Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);

        if (trainee.isEmpty()) {
            logger.warn("Trainee {} not found, transactionID={}", username, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Trainee not found"));
        }

        gymFacade.deleteTrainee(trainee.get().getUserId());

        logger.info("Trainee {} deleted successfully, transactionID={}", username, MDC.get("transactionID"));
        return ResponseEntity.ok(Map.of("message", "Trainee deleted successfully"));

    }

    @PatchMapping("/activate")
    @Operation(
            summary = "Activate or deactivate trainee (only for self)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Trainee active status updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to change other trainee's status",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Trainee not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    )
            }
    )
    public ResponseEntity<?> activateTrainee(
            @RequestParam String username,
            @RequestParam boolean isActive,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        logger.info("PATCH /trainee/activate called for {}, transactionID={}", username, MDC.get("transactionID"));

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("User {} tried to change active status for {}, transactionID={}", user.getUsername(), username, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only change your own active status"));
        }

        Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);

        if (trainee.isEmpty()) {
            logger.warn("Trainee {} not found, transactionID={}", username, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Trainee not found"));
        }

        trainee.get().setActive(isActive);
        gymFacade.updateTrainee(trainee.get());

        logger.info("Trainee {} active status updated to {}, transactionID={}", username, isActive, MDC.get("transactionID"));
        return ResponseEntity.ok(Map.of("message", "Trainee active status updated successfully"));

    }

    @GetMapping("/not-assigned")
    @Operation(
            summary = "Get trainers not assigned to the trainee (only self)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returns a set of trainers not assigned to trainee",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized access",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Trainee not found",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> getNotAssignedTrainers(
            @RequestParam String username,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        logger.info("GET /trainee/not-assigned called for {}, transactionID={}", username, MDC.get("transactionID"));

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("You are not logged in as user: {}, transactionID={}", username, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);
        if (trainee.isEmpty()) {
            logger.warn("Trainee {} not found, transactionID={}", username, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var trainers = gymFacade.getUnsignedTrainers(username).stream()
                .filter(User::isActive).map(TrainerDTO::new).collect(Collectors.toSet());

        return ResponseEntity.ok(Map.of("Trainers", trainers));
    }

    @GetMapping("/trainings")
    @Operation(
            summary = "Get trainee trainings with optional filters (only self)",
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
                            description = "Trainee not found",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> getTraineeTrainings(
            @RequestParam String username,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        logger.info("GET /trainee/trainings called for {}, transactionID={}", username, MDC.get("transactionID"));

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("You are not logged in as user: {}, transactionID={}", username, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var trainee = gymFacade.selectByTraineeName(username);
        if (trainee.isEmpty()) {
            logger.warn("Trainee {} not found, transactionID={}", username, MDC.get("transactionID"));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var trainings = Optional.ofNullable(gymFacade.getTraineeTrainings(username, trainerName, periodFrom, periodTo))
                .orElse(List.of())
                .stream()
                .map(TrainingDTO::new)
                .toList();

        return ResponseEntity.ok(Map.of("Trainings", trainings));
    }

}
