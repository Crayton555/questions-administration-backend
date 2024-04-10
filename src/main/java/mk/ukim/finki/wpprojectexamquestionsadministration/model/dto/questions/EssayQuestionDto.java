package mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations.FormatType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EssayQuestionDto {
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
    private String responseFormat;
    private boolean responseRequired;
    private int responseFieldLines;
    private int minWordLimit;
    private int maxWordLimit;
    private int attachments;
    private int attachmentsRequired;
    private long maxBytes;
    private List<String> fileTypesList;
    private String graderInfo;
    private FormatType graderInfoFormat;
    private String responseTemplate;
    private FormatType responseTemplateFormat;
    private Long categoryId;
    private List<Long> labelIds;
}