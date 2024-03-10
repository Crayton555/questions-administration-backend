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
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@AllArgsConstructor
@Entity
public abstract class BaseQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String questionType;
    private String name;

    @Column(length = 100000)
    private String questionText;

    private String generalFeedback;

    private double penalty;

    private boolean hidden = false;

    private String idNumber;

    @ManyToOne(optional = true)
    private Category category;

    @ManyToMany
    private List<Label> labels = new ArrayList<>();
}