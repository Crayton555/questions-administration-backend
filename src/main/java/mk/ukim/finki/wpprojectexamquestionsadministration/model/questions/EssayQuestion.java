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
public class EssayQuestion extends BaseQuestion {
    private double defaultGrade;
    private String responseFormat;
    private boolean responseRequired = false;
    private int responseFieldLines;
    private int minWordLimit;
    private int maxWordLimit;
    private int attachments;
    private int attachmentsRequired;
    private long maxBytes;
    @ElementCollection
    @CollectionTable(name = "essay_question_file_types", joinColumns = @JoinColumn(name = "essay_question_id"))
    @Column(name = "file_type")
    private List<String> fileTypesList = new ArrayList<>();
    private String graderInfo;
    private String responseTemplate;
}