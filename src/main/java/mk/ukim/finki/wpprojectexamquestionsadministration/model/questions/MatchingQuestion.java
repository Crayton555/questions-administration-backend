package mk.ukim.finki.wpprojectexamquestionsadministration.model.questions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String partiallyCorrectFeedback;
    private String incorrectFeedback;
    private boolean showNumCorrect = false;

    @ElementCollection
    private List<SubQuestion> subQuestions = new ArrayList<>();

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubQuestion {
        private String text;
        private String answer;
    }
}