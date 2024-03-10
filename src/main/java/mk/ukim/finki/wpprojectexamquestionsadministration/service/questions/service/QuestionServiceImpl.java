package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.service;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.LabelDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.CategoryRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.LabelRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.QuestionRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy.QuestionStrategy;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuestionServiceImpl<T extends BaseQuestion, D> implements IQuestionService<T, D> {
    private final Map<Class<?>, QuestionStrategy<? extends BaseQuestion, ?>> strategies;
    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final LabelRepository labelRepository;

    public QuestionServiceImpl(List<QuestionStrategy<? extends BaseQuestion, ?>> strategyList,
                               QuestionRepository questionRepository, CategoryRepository categoryRepository, LabelRepository labelRepository) {
        this.questionRepository = questionRepository;
        this.categoryRepository = categoryRepository;
        this.labelRepository = labelRepository;
        this.strategies = new HashMap<>();
        strategyList.forEach(strategy ->
                strategies.put(strategy.getQuestionDtoType(), strategy));
    }

    @Override
    public Optional<T> save(D questionDto) {
        QuestionStrategy<T, D> strategy = (QuestionStrategy<T, D>) strategies.get(questionDto.getClass());
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for question type: " + questionDto.getClass());
        }
        return strategy.save(questionDto);
    }

    @Override
    public Optional<T> edit(Long id, D questionDto) {
        QuestionStrategy<T, D> strategy = (QuestionStrategy<T, D>) strategies.get(questionDto.getClass());
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for question type: " + questionDto.getClass());
        }
        return strategy.edit(id, questionDto);
    }

    @Override
    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }

    @Override
    public Optional<T> findById(Long id) {
        return questionRepository.findById(id).map(question -> (T) question);
    }

    @Override
    public List<BaseQuestion> findAll() {
        return questionRepository.findAll();
    }

    @Override
    public Optional<T> changeQuestionCategory(Long questionId, Long newCategoryId) {
        Optional<BaseQuestion> questionOptional = questionRepository.findById(questionId);
        Optional<Category> categoryOptional = categoryRepository.findById(newCategoryId);

        if (questionOptional.isPresent() && categoryOptional.isPresent()) {
            BaseQuestion question = questionOptional.get();
            question.setCategory(categoryOptional.get());
            return Optional.of((T) questionRepository.save(question));
        }
        return Optional.empty();
    }

    @Override
    public void addNewLabelToQuestion(Long questionId, LabelDto labelDto) {
        BaseQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        Label label = new Label();
        label.setName(labelDto.getName());
        label = labelRepository.save(label);
        question.getLabels().add(label);
        questionRepository.save(question);
    }

    @Override
    public void processQuestionsFromXml(InputStream xmlData) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlData);
            doc.getDocumentElement().normalize();

            NodeList questionList = doc.getElementsByTagName("question");
            for (int i = 0; i < questionList.getLength(); i++) {
                Node questionNode = questionList.item(i);
                if (questionNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element questionElement = (Element) questionNode;
                    String type = questionElement.getAttribute("type");

                    QuestionStrategy<? extends BaseQuestion, ?> strategy = findStrategyByType(type);
                    if (strategy != null) {
                        strategy.saveFromXml(questionElement);
                    } else {
                        // Handle unknown type or log it
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    private QuestionStrategy<? extends BaseQuestion, ?> findStrategyByType(String type) {
        for(QuestionStrategy<? extends BaseQuestion, ?> strategy : strategies.values()) {
            if(strategy.isResponsibleFor(type)) {
                return strategy;
            }
        }
        return null;
    }
}