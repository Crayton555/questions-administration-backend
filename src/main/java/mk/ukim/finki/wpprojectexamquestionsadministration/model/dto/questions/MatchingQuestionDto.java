package mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations.FormatType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchingQuestionDto {
    private Long id;
    private String questionType;
    private String name;
    private String questionText;
    private FormatType questionTextFormat;
    private String generalFeedback;
    private FormatType generalFeedbackFormat;
    private double penalty;
    private boolean hidden;
    private String idNumber;
    private double defaultGrade;
    private boolean shuffleAnswers;
    private String correctFeedback;
    private FormatType correctFeedbackFormat;
    private String partiallyCorrectFeedback;
    private FormatType partiallyCorrectFeedbackFormat;
    private String incorrectFeedback;
    private FormatType incorrectFeedbackFormat;
    private boolean showNumCorrect;

    private List<SubQuestionDto> subQuestions;
    private Long categoryId;
    private List<Long> labelIds;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubQuestionDto {
        private FormatType subQuestionFormat;
        private String text;
        private String answer;
    }
}