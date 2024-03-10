package mk.ukim.finki.wpprojectexamquestionsadministration.model.questions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MultiChoiceQuestion extends BaseQuestion {
    private double defaultGrade;
    private boolean single = false;
    private boolean shuffleAnswers = false;
    private String answerNumbering;
    private boolean showStandardInstruction = false;
    private String correctFeedback;
    private String partiallyCorrectFeedback;
    private String incorrectFeedback;

    @ElementCollection
    private List<Answer> answerOptions = new ArrayList<>();

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        private double fraction;
        private String text;
        private String feedback;
    }
}