package org.example.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.exceptions.ForbiddenOperationException;
import org.example.exceptions.NotFoundException;
import org.example.facade.GymFacade;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.requestBodies.LoginRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication API", description = "Operations for login and password management")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private GymFacade gymFacade;

    @PostMapping("/login")
    @Operation(
            summary = "Login with username and password",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login successful",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid username or password",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    )
            }
    )
    public Map<String, String> login(@RequestBody LoginRequest request) throws ForbiddenOperationException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("User {} logged in successfully, transactionID={}", request.getUsername(), MDC.get("transactionID"));
            return Map.of("message", "Login successful");

        } catch (BadCredentialsException e) {
            logger.warn("Invalid login attempt for user {}, transactionID={}", request.getUsername(), MDC.get("transactionID"));
            throw new ForbiddenOperationException("Invalid username or password");
        }
    }

    @PutMapping("/users/{username}/password")
    @Operation(
            summary = "Change password (user must be logged in as themselves)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Password changed successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to change another user's password",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    )
            }
    )
    public Map<String, String> changePassword(
            @PathVariable String username,
            @RequestParam String newPassword,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) throws ForbiddenOperationException, NotFoundException {

        if (!username.equals(user.getUsername())) {
            logger.warn("User {} tried to change password for {}, transactionID={}", user.getUsername(), username, MDC.get("transactionID"));
            throw new ForbiddenOperationException("You can only change your own password");
        }

        if(user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_TRAINEE"))){
            Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);

            if(trainee.isEmpty()){
                logger.warn("Trainee {} not found, transactionID={}", username, MDC.get("transactionID"));
                throw new NotFoundException("Trainee not found");
            }

            trainee.get().setPassword(newPassword);
            gymFacade.updateTrainee(trainee.get());

        } else {
            Optional<Trainer> trainer = gymFacade.selectTrainerByUserName(username);

            if(trainer.isEmpty()){
                logger.warn("Trainer {} not found, transactionID={}", username, MDC.get("transactionID"));
                throw new NotFoundException("Trainer not found");
            }

            trainer.get().setPassword(newPassword);
            gymFacade.updateTrainer(trainer.get());
        }

        logger.info("Password for user {} changed successfully, transactionID={}", username, MDC.get("transactionID"));
        return Map.of("message", "Password changed successfully");
    }
}
