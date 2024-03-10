package mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortAnswerQuestionDto {
    private Long id;
    private String questionType;
    private String name;
    private String questionText;
    private String generalFeedback;
    private double penalty;
    private boolean hidden;
    private String idNumber;
    private double defaultGrade;
    private boolean useCase;
    private AnswerDto answer;
    private Long categoryId;
    private List<Long> labelIds;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDto {
        private String text;
        private double fraction;
        private String feedback;
    }
}