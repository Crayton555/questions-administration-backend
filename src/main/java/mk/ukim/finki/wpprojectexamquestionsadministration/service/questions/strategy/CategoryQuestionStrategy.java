package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.CategoryQuestionDto;
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

    public CategoryQuestionStrategy(QuestionRepository questionRepository,
                                    CategoryRepository categoryRepository,
                                    LabelRepository labelRepository) {
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
        return questionRepository.findById(id)
                .filter(question -> question instanceof CategoryQuestion)
                .map(question -> (CategoryQuestion) question);
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

        Category category = categoryRepository.findById(questionDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
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

    @Override
    public Optional<CategoryQuestion> saveFromXml(Element questionElement) {
        //String name = getTextContentByTagName(questionElement, "name");
        String name = getTextContentByTagName(questionElement, "category");
        //String questionText = getTextContentByTagName(questionElement, "questiontext");
        String questionText = getTextContentByTagName(questionElement, "info");
        String generalFeedback = getTextContentByTagName(questionElement, "generalfeedback");
        String penaltyText = getTextContentByTagName(questionElement, "penalty").trim();
        double penalty = 0.0;
        if (!penaltyText.isEmpty()) {
            try {
                penalty = Double.parseDouble(penaltyText);
            } catch (NumberFormatException e) {
                System.out.println("Invalid format for penalty, using default value: " + penalty);
            }
        }
        String hiddenText = getTextContentByTagName(questionElement, "hidden");
        boolean hidden = "1".equals(hiddenText);
        String idNumber = getTextContentByTagName(questionElement, "idnumber");
        // categoryText = getTextContentByTagName(questionElement, "category");
        //String infoText = getTextContentByTagName(questionElement, "info");

        CategoryQuestion question = new CategoryQuestion();
        question.setQuestionType("CategoryQuestion");
        question.setName(name);
        question.setQuestionText(questionText);
        question.setGeneralFeedback(generalFeedback);
        question.setPenalty(penalty);
        question.setHidden(hidden);
        question.setIdNumber(idNumber);
        //question.setCategoryText(categoryText);
        //question.setInfoText(infoText);

        Category defaultCategory = categoryRepository.findAll().get(0);
        question.setCategory(defaultCategory);

        NodeList tagsList = questionElement.getElementsByTagName("tag");
        for (int i = 0; i < tagsList.getLength(); i++) {
            Node tagNode = tagsList.item(i);
            if (tagNode.getNodeType() == Node.ELEMENT_NODE) {
                Element tagElement = (Element) tagNode;
                String tagText = tagElement.getTextContent();
                if (tagText != null && !tagText.trim().isEmpty()) {
                    Label label = labelRepository.findByName(tagText)
                            .orElseGet(() -> labelRepository.save(new Label(tagText)));
                    question.getLabels().add(label);
                }
            }
        }

        return Optional.of(questionRepository.save(question));
    }

    private String getTextContentByTagName(Element element, String tagName) {
        NodeList elements = element.getElementsByTagName(tagName);
        if (elements != null && elements.getLength() > 0) {
            Node firstNode = elements.item(0);
            if (firstNode != null && firstNode.hasChildNodes()) {
                NodeList childNodes = firstNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if ("text".equals(child.getNodeName()) && child.getTextContent() != null) {
                        return child.getTextContent();
                    }
                }
            }
            return firstNode.getTextContent() != null ? firstNode.getTextContent() : "";
        }
        return "";
    }

    @Override
    public Element toXmlElement(CategoryQuestion question, Document doc) {
        // Create the root element for this question
        Element questionElement = doc.createElement("question");
        questionElement.setAttribute("type", "category");

        // Create and append the <category> element
        Element categoryElement = doc.createElement("category");
        Element categoryTextElement = doc.createElement("text");
        categoryTextElement.setTextContent(question.getCategory().getName());
        categoryElement.appendChild(categoryTextElement);
        questionElement.appendChild(categoryElement);

        // Create and append the <info> element
        Element infoElement = doc.createElement("info");
        infoElement.setAttribute("format", "html"); // Assuming format is always "html"
        Element infoTextElement = doc.createElement("text");
        infoTextElement.setTextContent(question.getInfoText() != null ? question.getInfoText() : "");
        infoElement.appendChild(infoTextElement);
        questionElement.appendChild(infoElement);

        // Create and append the <idnumber> element, if present
        if (question.getIdNumber() != null && !question.getIdNumber().isEmpty()) {
            Element idNumberElement = doc.createElement("idnumber");
            idNumberElement.setTextContent(question.getIdNumber());
            questionElement.appendChild(idNumberElement);
        } else {
            // Even if idNumber is null or empty, append an empty <idnumber> element as per the provided structure
            questionElement.appendChild(doc.createElement("idnumber"));
        }

        return questionElement;
    }
}