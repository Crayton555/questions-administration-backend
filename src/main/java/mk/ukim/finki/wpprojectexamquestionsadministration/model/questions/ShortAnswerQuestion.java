package mk.ukim.finki.wpprojectexamquestionsadministration.model.questions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ShortAnswerQuestion extends BaseQuestion {
    private double defaultGrade;
    private boolean useCase = false;

    @Embedded
    private Answer answer;

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        private String text;
        private double fraction;
        private String feedback;
    }
}