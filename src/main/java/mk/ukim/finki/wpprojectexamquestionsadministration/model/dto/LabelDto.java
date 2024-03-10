package mk.ukim.finki.wpprojectexamquestionsadministration.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class LabelDto {
    private String name;
    private List<Long> questionIds;

    public LabelDto() {
    }

    public LabelDto(String name) {
        this.name = name;
    }

    public LabelDto(String name, List<Long> questionIds) {
        this.name = name;
        this.questionIds = questionIds;
    }
}
