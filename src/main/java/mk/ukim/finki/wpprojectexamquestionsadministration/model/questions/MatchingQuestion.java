package mk.ukim.finki.wpprojectexamquestionsadministration.model.questions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations.FormatType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MatchingQuestion extends BaseQuestion {
    private double defaultGrade;
    private boolean shuffleAnswers = false;
    private String correctFeedback;
    @Enumerated(EnumType.STRING)
    private FormatType correctFeedbackFormat = FormatType.HTML;
    private String partiallyCorrectFeedback;
    @Enumerated(EnumType.STRING)
    private FormatType partiallyCorrectFeedbackFormat = FormatType.HTML;
    private String incorrectFeedback;
    @Enumerated(EnumType.STRING)
    private FormatType incorrectFeedbackFormat = FormatType.HTML;
    private boolean showNumCorrect = false;

    @ElementCollection
    private List<SubQuestion> subQuestions = new ArrayList<>();

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubQuestion {
        @Enumerated(EnumType.STRING)
        private FormatType subQuestionFormat = FormatType.HTML;
        private String text;
        private String answer;
    }
}