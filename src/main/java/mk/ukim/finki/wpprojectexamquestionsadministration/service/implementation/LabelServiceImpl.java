package mk.ukim.finki.wpprojectexamquestionsadministration.service.implementation;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.LabelDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.LabelRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.QuestionRepository;

import mk.ukim.finki.wpprojectexamquestionsadministration.service.interfaces.LabelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class LabelServiceImpl implements LabelService {
    private final LabelRepository labelRepository;
    private final QuestionRepository questionRepository;

    public LabelServiceImpl(LabelRepository labelRepository, QuestionRepository questionRepository) {
        this.labelRepository = labelRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public List<Label> findAll() {
        return this.labelRepository.findAll();
    }

    @Override
    public Optional<Label> findById(Long id) {
        return this.labelRepository.findById(id);
    }

    @Override
    @Transactional
    public Optional<Label> save(String name, List<Long> questionIds) {
        List<BaseQuestion> questions = this.questionRepository.findAllByIds(questionIds);

        Label label = new Label(name, questions);
        this.labelRepository.save(label);

        return Optional.of(label);
    }

    @Override
    public Optional<Label> save(LabelDto labelDto) {
        List<BaseQuestion> questions = this.questionRepository.findAllByIds(labelDto.getQuestionIds());

        Label label = new Label(labelDto.getName(), questions);
        this.labelRepository.save(label);

        return Optional.of(label);
    }

    @Override
    @Transactional
    public Optional<Label> edit(Long id, String name, List<Long> questionIds) {
        Label label = this.labelRepository.findById(id).orElseThrow(() -> new RuntimeException("Label not found"));

        label.setName(name);
        this.labelRepository.save(label);

        return Optional.of(label);
    }

    @Override
    public Optional<Label> edit(Long id, LabelDto labelDto) {
        Label label = this.labelRepository.findById(id).orElseThrow(() -> new RuntimeException("Label not found"));

        label.setName(labelDto.getName());
        this.labelRepository.save(label);

        return Optional.of(label);
    }

    @Override
    public void deleteById(Long id) {
        Label label = this.labelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Label not found"));

        List<BaseQuestion> questions = label.getQuestions();
        if (questions != null) {
            for (BaseQuestion question : questions) {
                question.getLabels().remove(label);
                this.questionRepository.save(question);
            }
        }

        this.labelRepository.delete(label);
    }
}