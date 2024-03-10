package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;
import org.w3c.dom.Element;

import java.util.Optional;

public interface QuestionStrategy<T extends BaseQuestion, D> {

    Optional<T> save(D questionDto);

    Optional<T> edit(Long id, D questionDto);

    void deleteById(Long id);

    Optional<T> findById(Long id);
    Class<T> getQuestionType();
    Class<D> getQuestionDtoType();
    boolean isResponsibleFor(String type);
    public Optional<T> saveFromXml(Element questionElement);
}