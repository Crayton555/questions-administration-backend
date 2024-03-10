package mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiChoiceQuestionDto {
    private Long id;
    private String questionType;
    private String name;
    private String questionText;
    private String generalFeedback;
    private double penalty;
    private boolean hidden;
    private String idNumber;
    private double defaultGrade;
    private boolean single;
    private boolean shuffleAnswers;
    private String answerNumbering;
    private boolean showStandardInstruction;
    private String correctFeedback;
    private String partiallyCorrectFeedback;
    private String incorrectFeedback;
    private List<AnswerDto> answerOptions;
    private Long categoryId;
    private List<Long> labelIds;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDto {
        private double fraction;
        private String text;
        private String feedback;
    }
}