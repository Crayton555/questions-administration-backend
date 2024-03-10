package mk.ukim.finki.wpprojectexamquestionsadministration.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class CategoryDto {
    private String name;
    private List<Long> questionIds;

    public CategoryDto() {
    }

    public CategoryDto(String name) {
        this.name = name;
    }

    public CategoryDto(String name, List<Long> questionIds) {
        this.name = name;
        this.questionIds = questionIds;
    }
}
