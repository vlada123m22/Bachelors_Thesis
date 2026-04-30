package com.example.timesaver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "form_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Integer questionId;

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



    public FormQuestion(Integer questionId, Integer questionNumber, QuestionType questionType, String question) {
        this.questionId = questionId;
        this.questionNumber = questionNumber;
        this.questionType = questionType;
        this.question = question;
    }

    public FormQuestion( Integer questionNumber, QuestionType questionType, String question, String checkboxOptions) {
        this.questionNumber = questionNumber;
        this.questionType = questionType;
        this.question = question;
        this.checkboxOptions = checkboxOptions;
    }


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