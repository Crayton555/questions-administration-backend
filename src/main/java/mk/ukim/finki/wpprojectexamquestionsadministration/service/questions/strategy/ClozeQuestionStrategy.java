package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.ClozeQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.ClozeQuestion;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.CategoryRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.LabelRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.QuestionRepository;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.Optional;

@Service
public class ClozeQuestionStrategy implements QuestionStrategy<ClozeQuestion, ClozeQuestionDto> {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final LabelRepository labelRepository;
    public ClozeQuestionStrategy(QuestionRepository questionRepository,
                                 CategoryRepository categoryRepository,
                                 LabelRepository labelRepository) {
        this.questionRepository = questionRepository;
        this.categoryRepository = categoryRepository;
        this.labelRepository = labelRepository;
    }

    @Override
    public Optional<ClozeQuestion> save(ClozeQuestionDto questionDto) {
        ClozeQuestion question = new ClozeQuestion();
        populateQuestionFields(question, questionDto);
        return Optional.of(questionRepository.save(question));
    }

    @Override
    public Optional<ClozeQuestion> edit(Long id, ClozeQuestionDto questionDto) {
        return questionRepository.findById(id).map(question -> {
            if (question instanceof ClozeQuestion) {
                populateQuestionFields((ClozeQuestion) question, questionDto);
                return (ClozeQuestion) questionRepository.save(question);
            }
            throw new IllegalArgumentException("Invalid question id or type");
        });
    }

    @Override
    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }

    @Override
    public Optional<ClozeQuestion> findById(Long id) {
        return questionRepository.findById(id)
                .filter(question -> question instanceof ClozeQuestion)
                .map(question -> (ClozeQuestion) question);
    }

    private void populateQuestionFields(ClozeQuestion question, ClozeQuestionDto questionDto) {
        question.setQuestionType(questionDto.getQuestionType());
        question.setName(questionDto.getName());
        question.setQuestionText(questionDto.getQuestionText());
        question.setGeneralFeedback(questionDto.getGeneralFeedback());
        question.setPenalty(questionDto.getPenalty());
        question.setHidden(questionDto.isHidden());
        question.setIdNumber(questionDto.getIdNumber());

        Category category = categoryRepository.findById(questionDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        question.setCategory(category);

        List<Label> labels = labelRepository.findAllByIds(questionDto.getLabelIds());
        question.setLabels(labels);
    }
    @Override
    public Class<ClozeQuestion> getQuestionType() {
        return ClozeQuestion.class;
    }
    @Override
    public  Class<ClozeQuestionDto> getQuestionDtoType(){
        return ClozeQuestionDto.class;
    }

    @Override
    public boolean isResponsibleFor(String type) {
        return "cloze".equals(type);
    }

    @Override
    public Optional<ClozeQuestion> saveFromXml(Element questionElement) {
        String name = getTextContentByTagName(questionElement, "name");
        String questionText = getTextContentByTagName(questionElement, "questiontext");
        String generalFeedback = getTextContentByTagName(questionElement, "generalfeedback");
        double penalty = Double.parseDouble(questionElement.getElementsByTagName("penalty").item(0).getTextContent());
        boolean hidden = questionElement.getElementsByTagName("hidden").item(0).getTextContent().equals("1");
        String idNumber = getTextContentByTagName(questionElement, "idnumber");

        ClozeQuestion question = new ClozeQuestion();
        question.setQuestionType("ClozeQuestion");
        question.setName(name);
        question.setQuestionText(questionText);
        question.setGeneralFeedback(generalFeedback);
        question.setPenalty(penalty);
        question.setHidden(hidden);
        question.setIdNumber(idNumber);

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
}