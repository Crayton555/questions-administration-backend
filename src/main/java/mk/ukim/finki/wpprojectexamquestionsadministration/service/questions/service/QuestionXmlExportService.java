package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.service;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.QuestionRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy.QuestionStrategy;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuestionXmlExportService {

    private final QuestionRepository questionRepository;
    private final Map<Class<? extends BaseQuestion>, QuestionStrategy<? extends BaseQuestion, ?>> strategies;

    public QuestionXmlExportService(QuestionRepository questionRepository, List<QuestionStrategy<? extends BaseQuestion, ?>> strategyList) {
        this.questionRepository = questionRepository;
        this.strategies = new HashMap<>();
        strategyList.forEach(strategy -> strategies.put(strategy.getQuestionType(), strategy));
    }

    public Document exportQuestionsToXml() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("quiz");
        doc.appendChild(rootElement);

        List<BaseQuestion> questions = questionRepository.findAll();
        System.out.println("Exporting " + questions.size() + " questions.");
        for (BaseQuestion question : questions) {
            // Cast needed due to the wildcard usage in the strategies map
            @SuppressWarnings("unchecked") QuestionStrategy<BaseQuestion, ?> strategy = (QuestionStrategy<BaseQuestion, ?>) strategies.get(question.getClass());
            if (strategy != null) {
                Element questionElement = strategy.toXmlElement(question, doc);
                rootElement.appendChild(questionElement);
            } else {
                System.out.println("No strategy found for question type: " + question.getClass());
            }
        }
        return doc;
    }
}