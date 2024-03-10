package mk.ukim.finki.wpprojectexamquestionsadministration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Label {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @JsonIgnore
    @ManyToMany(mappedBy = "labels")
    private List<BaseQuestion> questions;

    public Label() {
    }

    public Label(String name) {
        this.name = name;
        this.questions = new ArrayList<>();
    }

    public Label(String name, List<BaseQuestion> questions) {
        this.name = name;
        this.questions = questions;
    }
}
