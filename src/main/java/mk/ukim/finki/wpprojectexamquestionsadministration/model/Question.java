package mk.ukim.finki.wpprojectexamquestionsadministration.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(length = 100000)
    private String questionText;
    @Column(length = 100000)
    private String generalFeedback;
    private double penalty;
    private boolean hidden;
    private String idNumber;

    public Question() {
    }

    public Question(String name, String questionText) {
        this.name = name;
        this.questionText = questionText;
    }

    public Question(String name, String questionText, String generalFeedback, double penalty, boolean hidden, String idNumber) {
        this.name = name;
        this.questionText = questionText;
        this.generalFeedback = generalFeedback;
        this.penalty = penalty;
        this.hidden = hidden;
        this.idNumber = idNumber;
    }
}