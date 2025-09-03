package org.example.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.example.facade.GymFacade;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.model.Training;
import org.example.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class LoadInitialEntities {

    private static final Logger logger = LoggerFactory.getLogger(LoadInitialEntities.class);
    private final GymFacade gym;

    @Value("${storage.file}")
    private String storagePath;

    @Autowired
    public LoadInitialEntities(GymFacade gym){
        this.gym = gym;
    }

   // @PostConstruct
    public void getData() throws IOException {
        logger.info("getData() called, path={}", storagePath);

        InputStream is = null;

        try {
            if (storagePath == null) {
                logger.warn("No initial data resource found");
                return;
            }

            if (storagePath.startsWith("classpath:")) {
                String path = storagePath.substring("classpath:".length());
                is = getClass().getClassLoader().getResourceAsStream(path);
                if (is == null) {
                    logger.warn("Classpath resource not found: {}", path);
                    return;
                }
            } else {
                is = new java.io.FileInputStream(storagePath);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(is);

            if (root.has("trainers")) {
                for (JsonNode t : root.get("trainers")) {
                    Trainer trainer = new Trainer();
                    trainer.setFirstName(t.path("firstName").asText(""));
                    trainer.setLastName(t.path("lastName").asText(""));
                    trainer.setActive(t.path("active").asBoolean(true));
                    gym.createTrainer(trainer, t.path("specialization").asText(""));
                }
            }

            if (root.has("trainees")) {
                for (JsonNode t : root.get("trainees")) {
                    Trainee trainee = new Trainee();
                    trainee.setFirstName(t.path("firstName").asText(""));
                    trainee.setLastName(t.path("lastName").asText(""));
                    trainee.setActive(t.path("active").asBoolean(false));
                    gym.createTrainee(
                            trainee,
                            java.time.LocalDate.parse(t.path("dateOfBirth").asText("1970-01-01")),
                            t.path("address").asText("")
                    );

                    System.out.println("\n\n\n");
                    System.out.println(gym.selectByTraineeName(trainee.getFirstName() + "." +
                            trainee.getLastName()).get());

                }
            }

            if (root.has("trainings")) {
                for (JsonNode t : root.get("trainings")) {
                    TrainingType type = new TrainingType(
                            t.path("trainingType").path("trainingTypeName").asText("")
                    );

                    gym.createTrainingType(type);

                    Training training = new Training(
                            gym.selectTrainee(t.path("traineeId").asLong(0)).get(),
                            gym.selectTrainer(t.path("trainerId").asLong(0)).get(),
                            t.path("trainingName").asText(""),
                            type,
                            java.time.LocalDate.parse(t.path("trainingDate").asText("1970-01-01")),
                            t.path("trainingDuration").asInt(0)
                    );

                    gym.createTraining(training);
                }
            }

            logger.info("Storage initialized from file");

        } catch (IOException e) {
            logger.warn("Initial data was not loaded because of IOException", e);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

}
