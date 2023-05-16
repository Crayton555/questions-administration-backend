package mk.ukim.finki.wpprojectexamquestionsadministration.service;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Question;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.QuestionDto;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface QuestionService {

    List<Question> findAll();

    Optional<Question> findById(Long id);

    Optional<Question> findByName(String name);

    Optional<Question> save(String name, String questionText, String generalFeedback, double penalty, boolean hidden, String idNumber);

    Optional<Question> save(QuestionDto questionDto);

    Optional<Question> edit(Long id, String name, String questionText, String generalFeedback, double penalty, boolean hidden, String idNumber);

    Optional<Question> edit(Long id, QuestionDto questionDto);

    void deleteById(Long id);

    void saveQuestionsXML(InputStream xmlFile);
}