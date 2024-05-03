package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.EssayQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.MultiChoiceQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations.FormatType;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.MultiChoiceQuestion;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.CategoryRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.LabelRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.QuestionRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MultiChoiceQuestionStrategy implements QuestionStrategy<MultiChoiceQuestion, MultiChoiceQuestionDto> {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final LabelRepository labelRepository;

    public MultiChoiceQuestionStrategy(QuestionRepository questionRepository, CategoryRepository categoryRepository, LabelRepository labelRepository) {
        this.questionRepository = questionRepository;
        this.categoryRepository = categoryRepository;
        this.labelRepository = labelRepository;
    }

    @Override
    public Optional<MultiChoiceQuestion> save(MultiChoiceQuestionDto questionDto) {
        MultiChoiceQuestion question = new MultiChoiceQuestion();
        populateQuestionFields(question, questionDto);
        return Optional.of((MultiChoiceQuestion) questionRepository.save(question));
    }

    @Override
    public Optional<MultiChoiceQuestion> edit(Long id, MultiChoiceQuestionDto questionDto) {
        return questionRepository.findById(id).map(question -> {
            if (question instanceof MultiChoiceQuestion) {
                populateQuestionFields((MultiChoiceQuestion) question, questionDto);
                return (MultiChoiceQuestion) questionRepository.save(question);
            }
            throw new IllegalArgumentException("Invalid question id or type");
        });
    }

    @Override
    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }

    @Override
    public Optional<MultiChoiceQuestion> findById(Long id) {
        return questionRepository.findById(id).filter(question -> question instanceof MultiChoiceQuestion).map(question -> (MultiChoiceQuestion) question);
    }

    private void populateQuestionFields(MultiChoiceQuestion question, MultiChoiceQuestionDto questionDto) {
        question.setQuestionType(questionDto.getQuestionType());
        question.setName(questionDto.getName());
        question.setQuestionText(questionDto.getQuestionText());
        question.setGeneralFeedback(questionDto.getGeneralFeedback());
        question.setPenalty(questionDto.getPenalty());
        question.setHidden(questionDto.isHidden());
        question.setIdNumber(questionDto.getIdNumber());
        question.setDefaultGrade(questionDto.getDefaultGrade());
        question.setSingle(questionDto.isSingle());
        question.setShuffleAnswers(questionDto.isShuffleAnswers());
        question.setAnswerNumbering(questionDto.getAnswerNumbering());
        question.setShowStandardInstruction(questionDto.isShowStandardInstruction());
        question.setCorrectFeedback(questionDto.getCorrectFeedback());
        question.setPartiallyCorrectFeedback(questionDto.getPartiallyCorrectFeedback());
        question.setIncorrectFeedback(questionDto.getIncorrectFeedback());

        question.setQuestionTextFormat(questionDto.getQuestionTextFormat());
        question.setGeneralFeedbackFormat(questionDto.getGeneralFeedbackFormat());
        question.setCorrectFeedbackFormat(questionDto.getCorrectFeedbackFormat());
        question.setPartiallyCorrectFeedbackFormat(questionDto.getPartiallyCorrectFeedbackFormat());
        question.setIncorrectFeedbackFormat(questionDto.getIncorrectFeedbackFormat());

        if (questionDto.getAnswerOptions() != null) {
            List<MultiChoiceQuestion.Answer> answerOptions = questionDto.getAnswerOptions().stream().map(dto -> new MultiChoiceQuestion.Answer(dto.getFraction(), dto.getAnswerFormat(), dto.getText(), dto.getFeedback(), dto.getFeedbackFormat())).collect(Collectors.toList());
            question.setAnswerOptions(answerOptions);
        }

        Category category = categoryRepository.findById(questionDto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        question.setCategory(category);

        List<Label> labels = labelRepository.findAllByIds(questionDto.getLabelIds());
        question.setLabels(labels);
    }

    @Override
    public Class<MultiChoiceQuestion> getQuestionType() {
        return MultiChoiceQuestion.class;
    }

    @Override
    public Class<MultiChoiceQuestionDto> getQuestionDtoType() {
        return MultiChoiceQuestionDto.class;
    }

    @Override
    public boolean isResponsibleFor(String type) {
        return "multichoice".equals(type);
    }

    @Override
    public Optional<MultiChoiceQuestion> saveFromXml(Element questionElement) {
        MultiChoiceQuestion question = new MultiChoiceQuestion();
        question.setQuestionType("MultiChoiceQuestion");

        importBaseQuestionAttributes(questionElement, question, categoryRepository, labelRepository);

        question.setDefaultGrade(parseDouble(getTextContentByTagName(questionElement, "defaultgrade")));
        question.setSingle(parseBoolean(getTextContentByTagName(questionElement, "single")));
        question.setShuffleAnswers(parseBoolean(getTextContentByTagName(questionElement, "shuffleanswers")));
        question.setAnswerNumbering(getTextContentByTagName(questionElement, "answernumbering"));
        question.setShowStandardInstruction(parseBoolean(getTextContentByTagName(questionElement, "showstandardinstruction")));
        question.setCorrectFeedback(getTextContentByTagName(questionElement, "correctfeedback"));
        question.setCorrectFeedbackFormat(extractFormat(questionElement, "correctfeedback"));
        question.setPartiallyCorrectFeedback(getTextContentByTagName(questionElement, "partiallycorrectfeedback"));
        question.setPartiallyCorrectFeedbackFormat(extractFormat(questionElement, "partiallycorrectfeedback"));
        question.setIncorrectFeedback(getTextContentByTagName(questionElement, "incorrectfeedback"));
        question.setIncorrectFeedbackFormat(extractFormat(questionElement, "incorrectfeedback"));

        NodeList answerList = questionElement.getElementsByTagName("answer");
        List<MultiChoiceQuestion.Answer> answerOptions = new ArrayList<>();
        for (int i = 0; i < answerList.getLength(); i++) {
            Node answerNode = answerList.item(i);
            if (answerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element answerElement = (Element) answerNode;
                Double fraction = Double.valueOf(answerElement.getAttribute("fraction"));
                String text = getTextContentByTagName(answerElement, "text");
                String feedback = getTextContentByTagName(answerElement, "feedback");
                FormatType answerFormat = extractFormat(answerElement, "");
                FormatType feedbackFormat = extractFormat(answerElement, "feedback");

                MultiChoiceQuestion.Answer answer = new MultiChoiceQuestion.Answer(fraction, answerFormat, text, feedback, feedbackFormat);
                answerOptions.add(answer);
            }
        }
        question.setAnswerOptions(answerOptions);

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

    @Override
    public Element toXmlElement(MultiChoiceQuestion question, Document doc) {
        Element questionElement = QuestionStrategy.super.toXmlElement(question, doc);
        questionElement.setAttribute("type", "multichoice");

        addSimpleElement(questionElement, doc, "shuffleanswers", question.isShuffleAnswers() ? "true" : "false");
        addSimpleElement(questionElement, doc, "single", question.isSingle() ? "true" : "false");
        addSimpleElement(questionElement, doc, "answernumbering", question.getAnswerNumbering());
        addSimpleElement(questionElement, doc, "showstandardinstruction", question.isShowStandardInstruction() ? "1" : "0");

        addFeedbackElement(questionElement, doc, "correctfeedback", question.getCorrectFeedback(), question.getCorrectFeedbackFormat());
        addFeedbackElement(questionElement, doc, "partiallycorrectfeedback", question.getPartiallyCorrectFeedback(), question.getPartiallyCorrectFeedbackFormat());
        addFeedbackElement(questionElement, doc, "incorrectfeedback", question.getIncorrectFeedback(), question.getIncorrectFeedbackFormat());

        for (MultiChoiceQuestion.Answer answer : question.getAnswerOptions()) {
            Element answerElement = doc.createElement("answer");
            answerElement.setAttribute("fraction", String.valueOf(answer.getFraction()));
            answerElement.setAttribute("format", answer.getAnswerFormat().toString().toLowerCase());

            Element textElement = doc.createElement("text");
            if (requiresCdata(answer.getText())) {
                textElement.appendChild(doc.createCDATASection(answer.getText()));
            } else {
                textElement.appendChild(doc.createTextNode(answer.getText()));
            }
            answerElement.appendChild(textElement);

            if (answer.getFeedback() != null && !answer.getFeedback().isEmpty()) {
                Element feedbackElement = doc.createElement("feedback");
                feedbackElement.setAttribute("format", answer.getFeedbackFormat().toString().toLowerCase());
                Element feedbackTextElement = doc.createElement("text");
                if (requiresCdata(answer.getFeedback())) {
                    feedbackTextElement.appendChild(doc.createCDATASection(answer.getFeedback()));
                } else {
                    feedbackTextElement.appendChild(doc.createTextNode(answer.getFeedback()));
                }
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

    private void addFeedbackElement(Element parent, Document doc, String tagName, String feedback, FormatType formatType) {
        if (feedback != null && !feedback.isEmpty()) {
            Element feedbackElement = doc.createElement(tagName);
            feedbackElement.setAttribute("format", formatType.toString().toLowerCase());
            Element feedbackTextElement = doc.createElement("text");
            feedbackTextElement.appendChild(doc.createTextNode(feedback));
            feedbackElement.appendChild(feedbackTextElement);
            parent.appendChild(feedbackElement);
        }
    }
}