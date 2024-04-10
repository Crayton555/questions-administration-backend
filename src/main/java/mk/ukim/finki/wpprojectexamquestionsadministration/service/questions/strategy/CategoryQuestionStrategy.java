package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.CategoryQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations.FormatType;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.CategoryQuestion;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.CategoryRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.LabelRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.QuestionRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryQuestionStrategy implements QuestionStrategy<CategoryQuestion, CategoryQuestionDto> {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final LabelRepository labelRepository;

    public CategoryQuestionStrategy(QuestionRepository questionRepository, CategoryRepository categoryRepository, LabelRepository labelRepository) {
        this.questionRepository = questionRepository;
        this.categoryRepository = categoryRepository;
        this.labelRepository = labelRepository;
    }

    @Override
    public Optional<CategoryQuestion> save(CategoryQuestionDto questionDto) {
        CategoryQuestion question = new CategoryQuestion();
        populateQuestionFields(question, questionDto);
        return Optional.of(questionRepository.save(question));
    }

    @Override
    public Optional<CategoryQuestion> edit(Long id, CategoryQuestionDto questionDto) {
        return questionRepository.findById(id).map(question -> {
            if (question instanceof CategoryQuestion) {
                populateQuestionFields((CategoryQuestion) question, questionDto);
                return (CategoryQuestion) questionRepository.save(question);
            }
            throw new IllegalArgumentException("Invalid question id or type");
        });
    }

    @Override
    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }

    @Override
    public Optional<CategoryQuestion> findById(Long id) {
        return questionRepository.findById(id).filter(question -> question instanceof CategoryQuestion).map(question -> (CategoryQuestion) question);
    }

    private void populateQuestionFields(CategoryQuestion question, CategoryQuestionDto questionDto) {
        question.setQuestionType(questionDto.getQuestionType());
        question.setName(questionDto.getName());
        question.setQuestionText(questionDto.getQuestionText());
        question.setGeneralFeedback(questionDto.getGeneralFeedback());
        question.setPenalty(questionDto.getPenalty());
        question.setHidden(questionDto.isHidden());
        question.setIdNumber(questionDto.getIdNumber());
        question.setCategoryText(questionDto.getCategoryText());
        question.setInfoText(questionDto.getInfoText());

        question.setQuestionTextFormat(questionDto.getQuestionTextFormat());
        question.setGeneralFeedbackFormat(questionDto.getGeneralFeedbackFormat());
        question.setInfoTextFormat(questionDto.getInfoTextFormat());

        Category category = categoryRepository.findById(questionDto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        question.setCategory(category);

        List<Label> labels = labelRepository.findAllByIds(questionDto.getLabelIds());
        question.setLabels(labels);
    }

    @Override
    public Class<CategoryQuestion> getQuestionType() {
        return CategoryQuestion.class;
    }

    @Override
    public Class<CategoryQuestionDto> getQuestionDtoType() {
        return CategoryQuestionDto.class;
    }

    @Override
    public boolean isResponsibleFor(String type) {
        return "category".equals(type);
    }

    public Optional<CategoryQuestion> saveFromXml(Element questionElement) {
        NodeList categoryList = questionElement.getElementsByTagName("category");
        String categoryText = "";
        if (categoryList.getLength() > 0) {
            Element categoryElement = (Element) categoryList.item(0);
            categoryText = categoryElement.getTextContent();
        }

        NodeList infoList = questionElement.getElementsByTagName("info");
        String infoText = "";
        FormatType infoTextFormat = FormatType.HTML;
        if (infoList.getLength() > 0) {
            Element infoElement = (Element) infoList.item(0);
            infoText = infoElement.getTextContent();
            String format = infoElement.getAttribute("format");
            infoTextFormat = FormatType.valueOf(format.toUpperCase());
        }

        NodeList idNumberList = questionElement.getElementsByTagName("idnumber");
        String idNumber = "";
        if (idNumberList.getLength() > 0) {
            idNumber = idNumberList.item(0).getTextContent();
        }

        CategoryQuestion question = new CategoryQuestion();
        question.setCategoryText(categoryText);
        question.setInfoText(infoText);
        question.setInfoTextFormat(infoTextFormat);
        question.setIdNumber(idNumber);
        question.setName(categoryText);
        question.setQuestionText(infoText);

        Category defaultCategory = categoryRepository.findAll().get(0);
        question.setCategory(defaultCategory);

        return Optional.of(questionRepository.save(question));
    }

    public Element toXmlElement(CategoryQuestion question, Document doc) {
        Element questionElement = doc.createElement("question");
        questionElement.setAttribute("type", "category");

        Element categoryElement = doc.createElement("category");
        questionElement.appendChild(categoryElement);

        Element categoryTextElement = doc.createElement("text");
        if (requiresCdata(question.getCategoryText())) {
            categoryTextElement.appendChild(doc.createCDATASection(question.getCategoryText()));
        } else {
            categoryTextElement.appendChild(doc.createTextNode(question.getCategoryText()));
        }
        categoryElement.appendChild(categoryTextElement);

        if (question.getInfoText() != null && !question.getInfoText().isEmpty()) {
            Element infoElement = doc.createElement("info");
            infoElement.setAttribute("format", question.getInfoTextFormat().toString().toLowerCase());
            Element infoTextElement = doc.createElement("text");
            if (requiresCdata(question.getInfoText())) {
                infoTextElement.appendChild(doc.createCDATASection(question.getInfoText()));
            } else {
                infoTextElement.appendChild(doc.createTextNode(question.getInfoText()));
            }
            infoElement.appendChild(infoTextElement);
            questionElement.appendChild(infoElement);
        }

        if (question.getIdNumber() != null && !question.getIdNumber().isEmpty()) {
            Element idNumberElement = doc.createElement("idnumber");
            idNumberElement.appendChild(doc.createTextNode(question.getIdNumber()));
            questionElement.appendChild(idNumberElement);
        }

        return questionElement;
    }
}