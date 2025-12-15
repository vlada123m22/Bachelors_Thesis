package com.example.timesaver.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "form_questions")
@Data
public class FormQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private Integer questionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;

    @Column(nullable = false, length = 1000)
    private String question;

    //Divided by "|". Null if not a checkbox question
    @Column(name = "checkbox_options", length = 1000)
    private String checkboxOptions;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Override equals and hashCode to avoid issues with bidirectional relationships
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormQuestion)) return false;
        FormQuestion that = (FormQuestion) o;
        return questionId != null && questionId.equals(that.questionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}