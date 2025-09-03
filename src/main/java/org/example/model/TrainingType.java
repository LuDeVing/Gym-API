package org.example.model;


import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;

@Entity
@Table(name = "training_types")
@Immutable
public class TrainingType {

    @Id
    @Column(nullable = false, updatable = false)
    private String trainingTypeName;

    public TrainingType() {}

    public TrainingType(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    public String getTrainingTypeName() { return trainingTypeName; }
    public void setTrainingTypeName(String trainingTypeName) { this.trainingTypeName = trainingTypeName; }

}
