package mk.ukim.finki.wpprojectexamquestionsadministration.model.dto;

import lombok.Data;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations.FormatType;

import java.util.List;

@Data
public class CategoryDto {
    private Long id;
    private String name;
    private String info;
    private String infoTextFormat;
    private String idNumber;
    private Long parentId;
    private List<Long> questionIds;

    public CategoryDto() {
    }

    public CategoryDto(String name, String info, String infoTextFormat, String idNumber, Long parentId, List<Long> questionIds) {
        this.name = name;
        this.info = info;
        this.infoTextFormat = infoTextFormat;
        this.idNumber = idNumber;
        this.parentId = parentId;
        this.questionIds = questionIds;
    }
}