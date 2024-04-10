package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.EssayQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.ShortAnswerQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations.FormatType;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.ShortAnswerQuestion;
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
public class ShortAnswerQuestionStrategy implements QuestionStrategy<ShortAnswerQuestion, ShortAnswerQuestionDto> {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final LabelRepository labelRepository;

    public ShortAnswerQuestionStrategy(QuestionRepository questionRepository, CategoryRepository categoryRepository, LabelRepository labelRepository) {
        this.questionRepository = questionRepository;
        this.categoryRepository = categoryRepository;
        this.labelRepository = labelRepository;
    }

    @Override
    public Optional<ShortAnswerQuestion> save(ShortAnswerQuestionDto questionDto) {
        ShortAnswerQuestion question = new ShortAnswerQuestion();
        populateQuestionFields(question, questionDto);
        return Optional.of((ShortAnswerQuestion) questionRepository.save(question));
    }

    @Override
    public Optional<ShortAnswerQuestion> edit(Long id, ShortAnswerQuestionDto questionDto) {
        return questionRepository.findById(id).map(question -> {
            if (question instanceof ShortAnswerQuestion) {
                populateQuestionFields((ShortAnswerQuestion) question, questionDto);
                return (ShortAnswerQuestion) questionRepository.save(question);
            }
            throw new IllegalArgumentException("Invalid question id or type");
        });
    }

    @Override
    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }

    @Override
    public Optional<ShortAnswerQuestion> findById(Long id) {
        return questionRepository.findById(id).filter(question -> question instanceof ShortAnswerQuestion).map(question -> (ShortAnswerQuestion) question);
    }

    private void populateQuestionFields(ShortAnswerQuestion question, ShortAnswerQuestionDto questionDto) {
        question.setQuestionType(questionDto.getQuestionType());
        question.setName(questionDto.getName());
        question.setQuestionText(questionDto.getQuestionText());
        question.setGeneralFeedback(questionDto.getGeneralFeedback());
        question.setPenalty(questionDto.getPenalty());
        question.setHidden(questionDto.isHidden());
        question.setIdNumber(questionDto.getIdNumber());
        question.setDefaultGrade(questionDto.getDefaultGrade());
        question.setUseCase(questionDto.isUseCase());
        question.setAnswer(new ShortAnswerQuestion.Answer(questionDto.getAnswer().getFraction(), questionDto.getAnswer().getAnswerFormat(), questionDto.getAnswer().getText(), questionDto.getAnswer().getFeedback(), questionDto.getAnswer().getFeedbackFormat()));

        Category category = categoryRepository.findById(questionDto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        question.setCategory(category);

        List<Label> labels = labelRepository.findAllByIds(questionDto.getLabelIds());
        question.setLabels(labels);
    }

    @Override
    public Class<ShortAnswerQuestion> getQuestionType() {
        return ShortAnswerQuestion.class;
    }

    @Override
    public Class<ShortAnswerQuestionDto> getQuestionDtoType() {
        return ShortAnswerQuestionDto.class;
    }

    @Override
    public boolean isResponsibleFor(String type) {
        return "shortanswer".equals(type);
    }

    @Override
    public Optional<ShortAnswerQuestion> saveFromXml(Element questionElement) {
        String name = getTextContentByTagName(questionElement, "text");
        String questionText = getTextContentByTagName(questionElement, "questiontext");
        FormatType questionTextFormat = extractFormat(questionElement, "questiontext"); // Assuming extractFormat method is implemented
        String generalFeedback = getTextContentByTagName(questionElement, "generalfeedback");
        FormatType generalFeedbackFormat = extractFormat(questionElement, "generalfeedback"); // Assuming extractFormat method is implemented
        String defaultGradeStr = getTextContentByTagName(questionElement, "defaultgrade");
        String penaltyStr = getTextContentByTagName(questionElement, "penalty");
        String idNumber = getTextContentByTagName(questionElement, "idnumber");
        String useCaseStr = getTextContentByTagName(questionElement, "usecase");

        ShortAnswerQuestion question = new ShortAnswerQuestion();
        question.setName(name);
        question.setQuestionText(questionText);
        question.setQuestionTextFormat(questionTextFormat);
        question.setGeneralFeedback(generalFeedback);
        question.setGeneralFeedbackFormat(generalFeedbackFormat);
        question.setDefaultGrade(Double.parseDouble(defaultGradeStr));
        question.setPenalty(Double.parseDouble(penaltyStr));
        question.setIdNumber(idNumber);
        question.setUseCase("1".equals(useCaseStr));

        NodeList answerList = questionElement.getElementsByTagName("answer");
        if (answerList.getLength() > 0) {
            Element answerElement = (Element) answerList.item(0);
            String fractionStr = answerElement.getAttribute("fraction");
            String answerText = getTextContentByTagName(answerElement, "text");
            String feedback = getTextContentByTagName(answerElement, "feedback");
            FormatType answerFormat = extractFormat(answerElement, ""); // Assuming extractFormat method is implemented for answers
            FormatType feedbackFormat = extractFormat(answerElement, "feedback"); // Assuming extractFormat method is implemented for feedback

            ShortAnswerQuestion.Answer answer = new ShortAnswerQuestion.Answer(Double.parseDouble(fractionStr), answerFormat, answerText, feedback, feedbackFormat);

            question.setAnswer(answer);
        }

        question.setQuestionType("ShortAnswerQuestion");

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

    private FormatType extractFormat(Element questionElement, String elementName) {
        NodeList nodeList = questionElement.getElementsByTagName(elementName);
        if (nodeList.getLength() > 0) {
            Element element = (Element) nodeList.item(0);
            String format = element.getAttribute("format");
            switch (format) {
                case "html" -> {
                    return FormatType.HTML;
                }
                case "moodle_auto_format" -> {
                    return FormatType.MOODLE_AUTO_FORMAT;
                }
                case "plain_text" -> {
                    return FormatType.PLAIN_TEXT;
                }
                case "markdown" -> {
                    return FormatType.MARKDOWN;
                }
            }
        }
        return FormatType.HTML;
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
    public Element toXmlElement(ShortAnswerQuestion question, Document doc) {
        Element questionElement = doc.createElement("question");
        questionElement.setAttribute("type", "shortanswer");

        Element nameElement = doc.createElement("name");
        Element nameTextElement = doc.createElement("text");
        nameTextElement.appendChild(doc.createTextNode(question.getName()));
        nameElement.appendChild(nameTextElement);
        questionElement.appendChild(nameElement);

        Element questionTextElement = doc.createElement("questiontext");
        questionTextElement.setAttribute("format", question.getQuestionTextFormat().toString().toLowerCase());
        Element questionTextContent = doc.createElement("text");
        if (requiresCdata(question.getQuestionText())) {
            questionTextContent.appendChild(doc.createCDATASection(question.getQuestionText()));
        } else {
            questionTextContent.appendChild(doc.createTextNode(question.getQuestionText()));
        }
        questionTextElement.appendChild(questionTextContent);
        questionElement.appendChild(questionTextElement);

        if (question.getGeneralFeedback() != null && !question.getGeneralFeedback().isEmpty()) {
            Element generalFeedbackElement = doc.createElement("generalfeedback");
            generalFeedbackElement.setAttribute("format", question.getGeneralFeedbackFormat().toString().toLowerCase());
            Element generalFeedbackContent = doc.createElement("text");
            if (requiresCdata(question.getGeneralFeedback())) {
                generalFeedbackContent.appendChild(doc.createCDATASection(question.getGeneralFeedback()));
            } else {
                generalFeedbackContent.appendChild(doc.createTextNode(question.getGeneralFeedback()));
            }
            generalFeedbackElement.appendChild(generalFeedbackContent);
            questionElement.appendChild(generalFeedbackElement);
        }

        addSimpleElement(questionElement, doc, "defaultgrade", String.valueOf(question.getDefaultGrade()));
        addSimpleElement(questionElement, doc, "penalty", String.valueOf(question.getPenalty()));
        addSimpleElement(questionElement, doc, "hidden", question.isHidden() ? "1" : "0");
        addSimpleElement(questionElement, doc, "usecase", question.isUseCase() ? "1" : "0");

        if (question.getAnswer() != null) {
            Element answerElement = doc.createElement("answer");
            answerElement.setAttribute("fraction", String.valueOf(question.getAnswer().getFraction()));
            answerElement.setAttribute("format", question.getAnswer().getAnswerFormat().toString().toLowerCase());

            Element textElement = doc.createElement("text");
            textElement.appendChild(doc.createTextNode(question.getAnswer().getText()));
            answerElement.appendChild(textElement);

            if (question.getAnswer().getFeedback() != null && !question.getAnswer().getFeedback().isEmpty()) {
                Element feedbackElement = doc.createElement("feedback");
                feedbackElement.setAttribute("format", question.getAnswer().getFeedbackFormat().toString().toLowerCase());
                Element feedbackTextElement = doc.createElement("text");
                feedbackTextElement.appendChild(doc.createTextNode(question.getAnswer().getFeedback()));
                feedbackElement.appendChild(feedbackTextElement);
                answerElement.appendChild(feedbackElement);
            }

            questionElement.appendChild(answerElement);
        }

        return questionElement;
    }

    private void addSimpleElement(Element parent, Document doc, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.appendChild(doc.createTextNode(textContent));
        parent.appendChild(element);
    }
}