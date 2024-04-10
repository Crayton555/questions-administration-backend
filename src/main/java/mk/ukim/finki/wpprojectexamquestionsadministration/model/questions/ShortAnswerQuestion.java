package mk.ukim.finki.wpprojectexamquestionsadministration.model.questions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations.FormatType;

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
        private double fraction;
        @Enumerated(EnumType.STRING)
        private FormatType answerFormat = FormatType.HTML;
        private String text;
        private String feedback;
        @Enumerated(EnumType.STRING)
        private FormatType feedbackFormat = FormatType.HTML;
    }
}