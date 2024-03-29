package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.EssayQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.ShortAnswerQuestionDto;
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
        question.setAnswer(new ShortAnswerQuestion.Answer(questionDto.getAnswer().getText(), questionDto.getAnswer().getFraction(), questionDto.getAnswer().getFeedback()));

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
        String generalFeedback = getTextContentByTagName(questionElement, "generalfeedback");
        String defaultGradeStr = getTextContentByTagName(questionElement, "defaultgrade");
        String penaltyStr = getTextContentByTagName(questionElement, "penalty");
        String idNumber = getTextContentByTagName(questionElement, "idnumber");
        String useCaseStr = getTextContentByTagName(questionElement, "usecase");

        ShortAnswerQuestion question = new ShortAnswerQuestion();
        question.setName(name);
        question.setQuestionText(questionText);
        question.setGeneralFeedback(generalFeedback);
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

            ShortAnswerQuestion.Answer answer = new ShortAnswerQuestion.Answer();
            answer.setText(answerText);
            answer.setFraction(Double.parseDouble(fractionStr));
            answer.setFeedback(feedback);

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

        // Question name
        Element nameElement = doc.createElement("name");
        Element nameTextElement = doc.createElement("text");
        nameTextElement.appendChild(doc.createTextNode(question.getName()));
        nameElement.appendChild(nameTextElement);
        questionElement.appendChild(nameElement);

        // Question text
        Element questionTextElement = doc.createElement("questiontext");
        questionTextElement.setAttribute("format", "html");
        Element questionTextTextElement = doc.createElement("text");
        // Wrap question text in CDATA
        questionTextTextElement.appendChild(doc.createCDATASection(question.getQuestionText()));
        questionTextElement.appendChild(questionTextTextElement);
        questionElement.appendChild(questionTextElement);

        // General feedback
        Element generalFeedbackElement = doc.createElement("generalfeedback");
        Element generalFeedbackTextElement = doc.createElement("text");
        // Wrap general feedback in CDATA
        generalFeedbackTextElement.appendChild(doc.createCDATASection(question.getGeneralFeedback()));
        generalFeedbackElement.appendChild(generalFeedbackTextElement);
        questionElement.appendChild(generalFeedbackElement);

        // Default grade
        Element defaultGradeElement = doc.createElement("defaultgrade");
        defaultGradeElement.appendChild(doc.createTextNode(String.valueOf(question.getDefaultGrade())));
        questionElement.appendChild(defaultGradeElement);

        // Penalty
        Element penaltyElement = doc.createElement("penalty");
        penaltyElement.appendChild(doc.createTextNode(String.valueOf(question.getPenalty())));
        questionElement.appendChild(penaltyElement);

        // Hidden
        Element hiddenElement = doc.createElement("hidden");
        hiddenElement.appendChild(doc.createTextNode(question.isHidden() ? "1" : "0"));
        questionElement.appendChild(hiddenElement);

        // Use case
        Element useCaseElement = doc.createElement("usecase");
        useCaseElement.appendChild(doc.createTextNode(question.isUseCase() ? "1" : "0"));
        questionElement.appendChild(useCaseElement);

        // Answer
        if (question.getAnswer() != null) {
            Element answerElement = doc.createElement("answer");
            answerElement.setAttribute("fraction", String.valueOf(question.getAnswer().getFraction()));
            answerElement.setAttribute("format", "moodle_auto_format");
            Element answerTextElement = doc.createElement("text");
            answerTextElement.appendChild(doc.createTextNode(question.getAnswer().getText()));
            answerElement.appendChild(answerTextElement);

            // Feedback for the answer
            Element feedbackElement = doc.createElement("feedback");
            Element feedbackTextElement = doc.createElement("text");
            // Wrap feedback in CDATA
            feedbackTextElement.appendChild(doc.createCDATASection(question.getAnswer().getFeedback()));
            feedbackElement.appendChild(feedbackTextElement);
            answerElement.appendChild(feedbackElement);

            questionElement.appendChild(answerElement);
        }

        return questionElement;
    }
}