package org.example.authorization;

import org.example.facade.GymFacade;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class mainUserDetailService implements UserDetailsService {

    @Autowired
    private GymFacade gymFacade;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);

        if (trainee.isPresent()) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(trainee.get().getUsername())
                    .password("{noop}" + trainee.get().getPassword())
                    .roles("TRAINEE")
                    .build();
        }

        Optional<Trainer> trainer = gymFacade.selectTrainerByUserName(username);

        if (trainer.isPresent()){
            return org.springframework.security.core.userdetails.User.builder()
                    .username(trainer.get().getUsername())
                    .password("{noop}" + trainer.get().getPassword())
                    .roles("TRAINER")
                    .build();
        }

        throw new UsernameNotFoundException("User not found");

    }
}
