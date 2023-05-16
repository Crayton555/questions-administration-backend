package mk.ukim.finki.wpprojectexamquestionsadministration.model.dto;

import lombok.Data;

import javax.persistence.Column;

@Data
public class QuestionDto {
    private String name;
    private String questionText;
    private String generalFeedback;
    private double penalty;
    private boolean hidden;
    private String idNumber;

    public QuestionDto() {
    }

    public QuestionDto(String name, String questionText) {
        this.name = name;
        this.questionText = questionText;
    }

    public QuestionDto(String name, String questionText, String generalFeedback, double penalty, boolean hidden, String idNumber) {
        this.name = name;
        this.questionText = questionText;
        this.generalFeedback = generalFeedback;
        this.penalty = penalty;
        this.hidden = hidden;
        this.idNumber = idNumber;
    }
}