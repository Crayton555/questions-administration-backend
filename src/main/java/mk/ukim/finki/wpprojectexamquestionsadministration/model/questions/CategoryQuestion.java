package mk.ukim.finki.wpprojectexamquestionsadministration.model.questions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations.FormatType;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CategoryQuestion extends BaseQuestion {
    private String categoryText;
    private String infoText;
    @Enumerated(EnumType.STRING)
    private FormatType infoTextFormat = FormatType.HTML;
}