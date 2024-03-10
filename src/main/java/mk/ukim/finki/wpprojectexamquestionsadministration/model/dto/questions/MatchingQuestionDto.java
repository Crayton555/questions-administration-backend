package mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchingQuestionDto {
    private Long id;
    private String questionType;
    private String name;
    private String questionText;
    private String generalFeedback;
    private double penalty;
    private boolean hidden;
    private String idNumber;
    private double defaultGrade;
    private boolean shuffleAnswers;
    private String correctFeedback;
    private String partiallyCorrectFeedback;
    private String incorrectFeedback;
    private boolean showNumCorrect;

    private List<SubQuestionDto> subQuestions;
    private Long categoryId;
    private List<Long> labelIds;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubQuestionDto {
        private String text;
        private String answer;
    }
}