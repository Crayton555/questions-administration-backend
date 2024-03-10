package mk.ukim.finki.wpprojectexamquestionsadministration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<BaseQuestion> questions;
    public Category() {
    }

    public Category(String name) {
        this.name = name;
        this.questions = new ArrayList<>();
    }

    public Category(String name, List<BaseQuestion> questions) {
        this.name = name;
        this.questions = questions;
    }
}
