package org.example.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.exceptions.ForbiddenOperationException;
import org.example.exceptions.NotFoundException;
import org.example.facade.GymFacade;
import org.example.model.Trainee;
import org.example.model.Training;
import org.example.model.User;
import org.example.requestBodies.CreateTraineeRequest;
import org.example.requestBodies.UpdateActiveRequest;
import org.example.requestBodies.UpdateTraineeRequest;
import org.example.responseBodies.*;
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
@RequestMapping(value = "trainees", produces = {"application/JSON"})
@Tag(name = "Trainee API", description = "Operations for creating, updating, retrieving and deleting trainees in application")
public class TraineeController {

    private static final Logger logger = LoggerFactory.getLogger(TraineeController.class);

    @Autowired
    private GymFacade gymFacade;

    @PostMapping
    @Operation(summary = "Add a new trainee, you don't need to be logged in as one",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
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
            @RequestBody CreateTraineeRequest request
    ) {

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);

        Trainee trainee = gymFacade.createTrainee(user, request.getDateOfBirth(), request.getAddress());

        Map<String, String> result = new HashMap<>();
        result.put("username", trainee.getUsername());
        result.put("password", trainee.getPassword());

        logger.info("new trainee with username: {} created, transactionID={}", trainee.getUsername(), MDC.get("transactionID"));

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainee info, if you are logged in as trainee",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully returned profile information",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TraineeWithTrainersDTO.class))
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
    public ResponseEntity<TraineeWithTrainersDTO> getTrainee(
            @PathVariable String username,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) throws ForbiddenOperationException {
        logger.info("GET /trainees/{} called, transactionID={}", username, MDC.get("transactionID"));

        if (!Objects.equals(username, user.getUsername())){
            logger.warn("You are not logged in as user: {}, transactionID={}", username, MDC.get("transactionID"));
            throw new ForbiddenOperationException("Forbidden to get as current user");
        }

        Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);

        if(trainee.isEmpty()){
            logger.warn("Trainee {} not found, returning 404, transactionID={}", username, MDC.get("transactionID"));
            return ResponseEntity.notFound().build();
        }

        logger.info("Returning 200 with trainee {}, transactionID={}", trainee.get().getUsername(), MDC.get("transactionID"));

        return ResponseEntity.ok(
                new TraineeWithTrainersDTO(new TraineeDTO(trainee.get()),
                        trainee.get().getTrainings().stream()
                                .map(Training::getTrainer)
                                .distinct()
                                .map(TrainerDTO::new)
                                .collect(Collectors.toSet())
                )
        );
    }

    @PutMapping("/{username}")
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
                            responseCode = "403",
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
    public ResponseEntity<TraineeDTO> updateTrainee(
            @PathVariable String username,
            @RequestBody UpdateTraineeRequest request,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) throws NotFoundException, ForbiddenOperationException {

        logger.info("PUT /trainees/{} called, transactionID={}", username, MDC.get("transactionID"));

        Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("You are not logged in as user: {}, cannot update, transactionID={}", username, MDC.get("transactionID"));
            throw new ForbiddenOperationException("Forbidden to get as current user");
        }

        if (trainee.isEmpty()) {
            logger.warn("Your username \"{}\" is not in database, transactionID={}", username, MDC.get("transactionID"));
            throw new NotFoundException("User not found");
        }

        Trainee t = trainee.get();
        t.setFirstName(request.getFirstName());
        t.setLastName(request.getLastName());
        t.setActive(request.isActive());

        if (request.getDateOfBirth() != null)
            t.setDateOfBirth(request.getDateOfBirth());

        if (request.getAddress() != null)
            t.setAddress(request.getAddress());

        gymFacade.updateTrainee(t);

        return ResponseEntity.ok(new TraineeDTO(t));
    }

    @DeleteMapping("/{username}")
    @Operation(
            summary = "delete trainee info if logged in as that trainee",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Successfully deleted trainee information",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
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
            @PathVariable String username,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) throws ForbiddenOperationException, NotFoundException {
        logger.info("DELETE /trainees/{} called, transactionID={}", username, MDC.get("transactionID"));

        if (!Objects.equals(username, user.getUsername())) {
            throw new ForbiddenOperationException("You can only access your own profile");
        }

        Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);

        if (trainee.isEmpty()) {
            throw new NotFoundException("Trainee does not exist");
        }

        gymFacade.deleteTrainee(trainee.get().getUserId());

        logger.info("Trainee {} deleted successfully, transactionID={}", username, MDC.get("transactionID"));
        return ResponseEntity.noContent().build();

    }

    @PatchMapping("/{username}/active")
    @Operation(
            summary = "Activate or deactivate trainee (only for self)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Trainee active status updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(type = "object", additionalPropertiesSchema = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to change other trainee's status",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Trainee not found",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public ResponseEntity<?> updateActiveStatus(
            @PathVariable String username,
            @RequestBody UpdateActiveRequest isActiveRequest,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) throws NotFoundException, ForbiddenOperationException {
        logger.info("PATCH /trainees/activate called for {}, transactionID={}", username, MDC.get("transactionID"));

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("User {} tried to change active status for {}, transactionID={}", user.getUsername(), username, MDC.get("transactionID"));
            throw new ForbiddenOperationException("Forbidden to get as current user");
        }

        Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);

        if (trainee.isEmpty()) {
            logger.warn("Trainee {} not found, transactionID={}", username, MDC.get("transactionID"));
            throw new NotFoundException("User not found");

        }

        trainee.get().setActive(isActiveRequest.getIsActive());
        gymFacade.updateTrainee(trainee.get());

        logger.info("Trainee {} active status updated to {}, transactionID={}", username, isActiveRequest.getIsActive(), MDC.get("transactionID"));
        return ResponseEntity.ok(Map.of("message", "Trainee active status updated successfully"));

    }

    @GetMapping("{username}/not-assigned-trainers")
    @Operation(
            summary = "Get trainers not assigned to the trainee (only self)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returns a set of trainers not assigned to trainee",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetTrainersResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
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
            @PathVariable String username,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) throws NotFoundException, ForbiddenOperationException {
        logger.info("GET /trainees/not-assigned called for {}, transactionID={}", username, MDC.get("transactionID"));

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("You are not logged in as user: {}, transactionID={}", username, MDC.get("transactionID"));
            throw new ForbiddenOperationException("Forbidden to get as current user");
        }

        Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);
        if (trainee.isEmpty()) {
            logger.warn("Trainee {} not found, transactionID={}", username, MDC.get("transactionID"));
            throw new NotFoundException("User not found");
        }

        var trainers = gymFacade.getUnsignedTrainers(username).stream()
                .filter(User::isActive).map(TrainerDTO::new).collect(Collectors.toSet());

        return ResponseEntity.ok(new GetTrainersResponse(trainers));
    }

    @GetMapping("{username}/trainings")
    @Operation(
            summary = "Get trainee trainings with optional filters (only self)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returns a list of trainings",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TraineeTrainingsResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
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
            @PathVariable String username,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) throws ForbiddenOperationException, NotFoundException {
        logger.info("GET /trainees/trainings called for {}, transactionID={}", username, MDC.get("transactionID"));

        if (!Objects.equals(username, user.getUsername())) {
            logger.warn("You are not logged in as user: {}, transactionID={}", username, MDC.get("transactionID"));
            throw new ForbiddenOperationException("Forbidden to get as current user");
        }

        var trainee = gymFacade.selectByTraineeName(username);
        if (trainee.isEmpty()) {
            logger.warn("Trainee {} not found, transactionID={}", username, MDC.get("transactionID"));
            throw new NotFoundException("User not found");
        }

        List<TrainingDTO> trainings = Optional.ofNullable(gymFacade.getTraineeTrainings(username, trainerName, periodFrom, periodTo))
                .orElse(List.of())
                .stream()
                .map(TrainingDTO::new)
                .toList();

        return ResponseEntity.ok(new TraineeTrainingsResponse(trainings));
    }

}
