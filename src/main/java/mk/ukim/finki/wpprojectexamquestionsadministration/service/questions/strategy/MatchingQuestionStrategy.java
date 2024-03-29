package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.EssayQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.MatchingQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.MatchingQuestion;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.CategoryRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.LabelRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.QuestionRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MatchingQuestionStrategy implements QuestionStrategy<MatchingQuestion, MatchingQuestionDto> {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final LabelRepository labelRepository;

    public MatchingQuestionStrategy(QuestionRepository questionRepository, CategoryRepository categoryRepository, LabelRepository labelRepository) {
        this.questionRepository = questionRepository;
        this.categoryRepository = categoryRepository;
        this.labelRepository = labelRepository;
    }

    @Override
    public Optional<MatchingQuestion> save(MatchingQuestionDto questionDto) {
        MatchingQuestion question = new MatchingQuestion();
        populateQuestionFields(question, questionDto);
        return Optional.of((MatchingQuestion) questionRepository.save(question));
    }

    @Override
    public Optional<MatchingQuestion> edit(Long id, MatchingQuestionDto questionDto) {
        return questionRepository.findById(id).map(question -> {
            if (question instanceof MatchingQuestion) {
                populateQuestionFields((MatchingQuestion) question, questionDto);
                return (MatchingQuestion) questionRepository.save(question);
            }
            throw new IllegalArgumentException("Invalid question id or type");
        });
    }

    @Override
    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }

    @Override
    public Optional<MatchingQuestion> findById(Long id) {
        return questionRepository.findById(id).filter(question -> question instanceof MatchingQuestion).map(question -> (MatchingQuestion) question);
    }

    private void populateQuestionFields(MatchingQuestion question, MatchingQuestionDto questionDto) {
        question.setQuestionType(questionDto.getQuestionType());
        question.setName(questionDto.getName());
        question.setQuestionText(questionDto.getQuestionText());
        question.setGeneralFeedback(questionDto.getGeneralFeedback());
        question.setPenalty(questionDto.getPenalty());
        question.setHidden(questionDto.isHidden());
        question.setIdNumber(questionDto.getIdNumber());
        question.setDefaultGrade(questionDto.getDefaultGrade());
        question.setShuffleAnswers(questionDto.isShuffleAnswers());
        question.setCorrectFeedback(questionDto.getCorrectFeedback());
        question.setPartiallyCorrectFeedback(questionDto.getPartiallyCorrectFeedback());
        question.setIncorrectFeedback(questionDto.getIncorrectFeedback());
        question.setShowNumCorrect(questionDto.isShowNumCorrect());
        question.setSubQuestions(questionDto.getSubQuestions().stream().map(dto -> new MatchingQuestion.SubQuestion(dto.getText(), dto.getAnswer())).collect(Collectors.toList()));

        Category category = categoryRepository.findById(questionDto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        question.setCategory(category);

        List<Label> labels = labelRepository.findAllByIds(questionDto.getLabelIds());
        question.setLabels(labels);
    }

    @Override
    public Class<MatchingQuestion> getQuestionType() {
        return MatchingQuestion.class;
    }

    @Override
    public Class<MatchingQuestionDto> getQuestionDtoType() {
        return MatchingQuestionDto.class;
    }

    @Override
    public boolean isResponsibleFor(String type) {
        return "matching".equals(type);
    }

    @Override
    public Optional<MatchingQuestion> saveFromXml(Element questionElement) {
        MatchingQuestion question = new MatchingQuestion();

        question.setQuestionType("MatchingQuestion");
        question.setName(getTextContentByTagName(questionElement, "name"));
        question.setQuestionText(getTextContentByTagName(questionElement, "questiontext"));
        question.setGeneralFeedback(getTextContentByTagName(questionElement, "generalfeedback"));
        question.setPenalty(parseDouble(getTextContentByTagName(questionElement, "penalty")));
        question.setHidden(parseBoolean(getTextContentByTagName(questionElement, "hidden")));
        question.setIdNumber(getTextContentByTagName(questionElement, "idnumber"));

        question.setDefaultGrade(parseDouble(getTextContentByTagName(questionElement, "defaultgrade")));
        question.setShuffleAnswers(parseBoolean(getTextContentByTagName(questionElement, "shuffleanswers")));
        question.setCorrectFeedback(getTextContentByTagName(questionElement, "correctfeedback"));
        question.setPartiallyCorrectFeedback(getTextContentByTagName(questionElement, "partiallycorrectfeedback"));
        question.setIncorrectFeedback(getTextContentByTagName(questionElement, "incorrectfeedback"));
        question.setShowNumCorrect(parseBoolean(getTextContentByTagName(questionElement, "shownumcorrect")));

        NodeList subQuestionList = questionElement.getElementsByTagName("subquestion");
        List<MatchingQuestion.SubQuestion> subQuestions = new ArrayList<>();
        for (int i = 0; i < subQuestionList.getLength(); i++) {
            Node subQuestionNode = subQuestionList.item(i);
            if (subQuestionNode.getNodeType() == Node.ELEMENT_NODE) {
                Element subQuestionElement = (Element) subQuestionNode;
                String text = getTextContentByTagName(subQuestionElement, "text");
                String answer = getTextContentByTagName(subQuestionElement, "answer");
                subQuestions.add(new MatchingQuestion.SubQuestion(text, answer));
            }
        }
        question.setSubQuestions(subQuestions);

        Category defaultCategory = categoryRepository.findAll().get(0);
        question.setCategory(defaultCategory);

        NodeList tagsList = questionElement.getElementsByTagName("tag");
        for (int i = 0; i < tagsList.getLength(); i++) {
            Node tagNode = tagsList.item(i);
            if (tagNode.getNodeType() == Node.ELEMENT_NODE) {
                Element tagElement = (Element) tagNode;
                String tagText = tagElement.getTextContent();
                if (tagText != null && !tagText.trim().isEmpty()) {
                    Label label = labelRepository.findByName(tagText).orElseGet(() -> labelRepository.save(new Label(tagText)));
                    question.getLabels().add(label);
                }
            }
        }

        return Optional.of(questionRepository.save(question));
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "1".equals(value);
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
    public Element toXmlElement(MatchingQuestion question, Document doc) {
        // Create the root question element
        Element questionElement = doc.createElement("question");
        questionElement.setAttribute("type", "matching");

        // Name
        Element nameElement = createTextElement(doc, "name", question.getName());
        questionElement.appendChild(nameElement);

        // Question text
        Element questionTextElement = createFormattedTextElement(doc, "questiontext", "html", question.getQuestionText());
        questionElement.appendChild(questionTextElement);

        // General feedback
        Element generalFeedbackElement = createFormattedTextElement(doc, "generalfeedback", "html", question.getGeneralFeedback());
        questionElement.appendChild(generalFeedbackElement);

        // Default grade
        questionElement.appendChild(createTextElement(doc, "defaultgrade", String.valueOf(question.getDefaultGrade())));

        // Penalty
        questionElement.appendChild(createTextElement(doc, "penalty", String.valueOf(question.getPenalty())));

        // Hidden
        questionElement.appendChild(createTextElement(doc, "hidden", question.isHidden() ? "1" : "0"));

        // Shuffle answers
        questionElement.appendChild(createTextElement(doc, "shuffleanswers", question.isShuffleAnswers() ? "true" : "false"));

        // Correct feedback
        Element correctFeedbackElement = createFormattedTextElement(doc, "correctfeedback", "html", question.getCorrectFeedback());
        questionElement.appendChild(correctFeedbackElement);

        // Partially correct feedback
        Element partiallyCorrectFeedbackElement = createFormattedTextElement(doc, "partiallycorrectfeedback", "html", question.getPartiallyCorrectFeedback());
        questionElement.appendChild(partiallyCorrectFeedbackElement);

        // Incorrect feedback
        Element incorrectFeedbackElement = createFormattedTextElement(doc, "incorrectfeedback", "html", question.getIncorrectFeedback());
        questionElement.appendChild(incorrectFeedbackElement);

        // Show num correct
        if (question.isShowNumCorrect()) {
            questionElement.appendChild(createTextElement(doc, "shownumcorrect", ""));
        }

        // Subquestions
        for (MatchingQuestion.SubQuestion subQuestion : question.getSubQuestions()) {
            Element subQuestionElement = doc.createElement("subquestion");
            subQuestionElement.setAttribute("format", "html");
            subQuestionElement.appendChild(createCDATAElement(doc, "text", subQuestion.getText()));

            Element answerElement = doc.createElement("answer");
            answerElement.appendChild(createCDATAElement(doc, "text", subQuestion.getAnswer()));

            subQuestionElement.appendChild(answerElement);
            questionElement.appendChild(subQuestionElement);
        }

        return questionElement;
    }

    private Element createTextElement(Document doc, String tagName, String text) {
        Element element = doc.createElement(tagName);
        element.appendChild(doc.createTextNode(text));
        return element;
    }

    private Element createFormattedTextElement(Document doc, String tagName, String format, String text) {
        Element element = doc.createElement(tagName);
        element.setAttribute("format", format);
        Element textElement = doc.createElement("text");
        textElement.appendChild(doc.createCDATASection(text));
        element.appendChild(textElement);
        return element;
    }

    private Element createCDATAElement(Document doc, String tagName, String cdataText) {
        Element textElement = doc.createElement(tagName);
        CDATASection cdataSection = doc.createCDATASection(cdataText);
        textElement.appendChild(cdataSection);
        return textElement;
    }
}