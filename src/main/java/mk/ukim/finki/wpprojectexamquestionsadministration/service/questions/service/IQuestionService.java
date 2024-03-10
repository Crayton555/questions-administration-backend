package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.service;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.LabelDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface IQuestionService<T extends BaseQuestion, D> {

    Optional<T> save(D questionDto);

    Optional<T> edit(Long id, D questionDto);

    void deleteById(Long id);

    Optional<T> findById(Long id);

    List<BaseQuestion> findAll();
    Optional<T> changeQuestionCategory(Long questionId, Long newCategoryId);
    void addNewLabelToQuestion(Long questionId, LabelDto labelDto);
    public void processQuestionsFromXml(InputStream xmlData);
}