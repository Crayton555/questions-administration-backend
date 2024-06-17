package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.service;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.CategoryRepository;
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
    private final CategoryRepository categoryRepository;
    private final Map<Class<? extends BaseQuestion>, QuestionStrategy<? extends BaseQuestion, ?>> strategies;

    public QuestionXmlExportService(QuestionRepository questionRepository, CategoryRepository categoryRepository, List<QuestionStrategy<? extends BaseQuestion, ?>> strategyList) {
        this.questionRepository = questionRepository;
        this.categoryRepository = categoryRepository;
        this.strategies = new HashMap<>();
        strategyList.forEach(strategy -> strategies.put(strategy.getQuestionType(), strategy));
    }

    public Document exportQuestionsToXml() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("quiz");
        doc.appendChild(rootElement);

        List<Category> rootCategories = categoryRepository.findRootCategories();
        for (Category category : rootCategories) {
            appendCategoryElement(doc, rootElement, category);
        }

        return doc;
    }

    private void appendCategoryElement(Document doc, Element parentElement, Category category) {
        Element categoryElement = doc.createElement("question");
        categoryElement.setAttribute("type", "category");

        Element categoryTextElement = doc.createElement("category");
        Element textElement = doc.createElement("text");

        String fullPath = getCategoryFullPath(category);
        textElement.appendChild(doc.createTextNode(fullPath));
        categoryTextElement.appendChild(textElement);
        categoryElement.appendChild(categoryTextElement);

        Element infoElement = doc.createElement("info");
        infoElement.setAttribute("format", category.getInfoTextFormat().name().toLowerCase());
        Element infoTextElement = doc.createElement("text");
        infoTextElement.appendChild(doc.createTextNode(category.getInfo() != null ? category.getInfo() : ""));
        infoElement.appendChild(infoTextElement);
        categoryElement.appendChild(infoElement);

        Element idNumberElement = doc.createElement("idnumber");
        idNumberElement.appendChild(doc.createTextNode(category.getIdNumber() != null ? category.getIdNumber() : ""));
        categoryElement.appendChild(idNumberElement);

        parentElement.appendChild(categoryElement);

        for (BaseQuestion question : category.getQuestions()) {
            appendQuestionElement(doc, parentElement, question);
        }

        List<Category> subcategories = categoryRepository.findByParentId(category.getId());
        for (Category subcategory : subcategories) {
            appendCategoryElement(doc, parentElement, subcategory);
        }
    }

    private String getCategoryFullPath(Category category) {
        if (category.getParentCategory() == null) {
            return category.getName();
        }
        return getCategoryFullPath(category.getParentCategory()) + "/" + category.getName();
    }

    private void appendQuestionElement(Document doc, Element parentElement, BaseQuestion question) {
        @SuppressWarnings("unchecked")
        QuestionStrategy<BaseQuestion, ?> strategy = (QuestionStrategy<BaseQuestion, ?>) strategies.get(question.getClass());
        if (strategy != null) {
            Element questionElement = strategy.toXmlElement(question, doc);
            parentElement.appendChild(questionElement);
        } else {
            System.out.println("No strategy found for question type: " + question.getClass());
        }
    }
}