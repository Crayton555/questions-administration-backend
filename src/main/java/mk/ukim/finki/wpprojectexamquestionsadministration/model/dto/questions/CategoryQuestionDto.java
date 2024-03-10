package mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryQuestionDto {
    private Long id;
    private String questionType;
    private String name;
    private String questionText;
    private String generalFeedback;
    private double penalty;
    private boolean hidden;
    private String idNumber;
    private String categoryText;
    private String infoText;
    private Long categoryId;
    private List<Long> labelIds;
}