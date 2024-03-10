package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.EssayQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.MultiChoiceQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.MultiChoiceQuestion;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.CategoryRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.LabelRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.QuestionRepository;
import org.springframework.stereotype.Service;
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

    public MultiChoiceQuestionStrategy(QuestionRepository questionRepository,
                                       CategoryRepository categoryRepository,
                                       LabelRepository labelRepository) {
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
        return questionRepository.findById(id)
                .filter(question -> question instanceof MultiChoiceQuestion)
                .map(question -> (MultiChoiceQuestion) question);
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
        question.setAnswerOptions(questionDto.getAnswerOptions().stream()
                .map(dto -> new MultiChoiceQuestion.Answer(dto.getFraction(), dto.getText(), dto.getFeedback()))
                .collect(Collectors.toList()));

        Category category = categoryRepository.findById(questionDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        question.setCategory(category);

        List<Label> labels = labelRepository.findAllByIds(questionDto.getLabelIds());
        question.setLabels(labels);
    }
    @Override
    public Class<MultiChoiceQuestion> getQuestionType() {
        return MultiChoiceQuestion.class;
    }
    @Override
    public  Class<MultiChoiceQuestionDto> getQuestionDtoType(){
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
        question.setName(getTextContentByTagName(questionElement, "name"));
        question.setQuestionText(getTextContentByTagName(questionElement, "questiontext"));
        question.setGeneralFeedback(getTextContentByTagName(questionElement, "generalfeedback"));
        question.setPenalty(parseDouble(getTextContentByTagName(questionElement, "penalty")));
        question.setHidden(parseBoolean(getTextContentByTagName(questionElement, "hidden")));
        question.setIdNumber(getTextContentByTagName(questionElement, "idnumber"));

        question.setDefaultGrade(parseDouble(getTextContentByTagName(questionElement, "defaultgrade")));
        question.setSingle(parseBoolean(getTextContentByTagName(questionElement, "single")));
        question.setShuffleAnswers(parseBoolean(getTextContentByTagName(questionElement, "shuffleanswers")));
        question.setAnswerNumbering(getTextContentByTagName(questionElement, "answernumbering"));
        question.setShowStandardInstruction(parseBoolean(getTextContentByTagName(questionElement, "showstandardinstruction")));
        question.setCorrectFeedback(getTextContentByTagName(questionElement, "correctfeedback"));
        question.setPartiallyCorrectFeedback(getTextContentByTagName(questionElement, "partiallycorrectfeedback"));
        question.setIncorrectFeedback(getTextContentByTagName(questionElement, "incorrectfeedback"));

        NodeList answerList = questionElement.getElementsByTagName("answer");
        List<MultiChoiceQuestion.Answer> answerOptions = new ArrayList<>();
        for (int i = 0; i < answerList.getLength(); i++) {
            Node answerNode = answerList.item(i);
            if (answerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element answerElement = (Element) answerNode;
                double fraction = parseDouble(answerElement.getAttribute("fraction"));
                String text = getTextContentByTagName(answerElement, "text");
                String feedback = getTextContentByTagName(answerElement, "feedback");
                answerOptions.add(new MultiChoiceQuestion.Answer(fraction, text, feedback));
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
                    Label label = labelRepository.findByName(tagText)
                            .orElseGet(() -> labelRepository.save(new Label(tagText)));
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
}