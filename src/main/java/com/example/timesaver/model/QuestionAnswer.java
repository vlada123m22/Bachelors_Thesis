package com.example.timesaver.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "question_answers")
@Data
public class QuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionAnswerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private FormQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @Column(nullable = false, length = 5000)
    private String questionAnswer;

    // Helper method to parse checkbox answers (stored as "option1|option2|option3")
    public String[] getCheckboxAnswers() {
        if (questionAnswer == null || questionAnswer.isEmpty()) {
            return new String[0];
        }
        return questionAnswer.split("\\|");
    }

    // Helper method to set checkbox answers
    public void setCheckboxAnswers(String[] answers) {
        if (answers == null || answers.length == 0) {
            this.questionAnswer = "";
        } else {
            this.questionAnswer = String.join("|", answers);
        }
    }
}