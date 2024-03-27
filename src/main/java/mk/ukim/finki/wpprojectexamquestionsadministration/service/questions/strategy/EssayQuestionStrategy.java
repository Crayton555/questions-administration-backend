package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.ClozeQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.EssayQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.EssayQuestion;
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

@Service
public class EssayQuestionStrategy implements QuestionStrategy<EssayQuestion, EssayQuestionDto> {
    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final LabelRepository labelRepository;

    public EssayQuestionStrategy(QuestionRepository questionRepository, CategoryRepository categoryRepository, LabelRepository labelRepository) {
        this.questionRepository = questionRepository;
        this.categoryRepository = categoryRepository;
        this.labelRepository = labelRepository;
    }

    @Override
    public Optional<EssayQuestion> save(EssayQuestionDto questionDto) {
        EssayQuestion question = new EssayQuestion();
        populateQuestionFields(question, questionDto);
        return Optional.of(questionRepository.save(question));
    }

    @Override
    public Optional<EssayQuestion> edit(Long id, EssayQuestionDto questionDto) {
        return questionRepository.findById(id).map(question -> {
            if (question instanceof EssayQuestion) {
                populateQuestionFields((EssayQuestion) question, questionDto);
                return (EssayQuestion) questionRepository.save(question);
            }
            throw new IllegalArgumentException("Invalid question id or type");
        });
    }

    @Override
    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }

    @Override
    public Optional<EssayQuestion> findById(Long id) {
        return questionRepository.findById(id).filter(question -> question instanceof EssayQuestion).map(question -> (EssayQuestion) question);
    }

    private void populateQuestionFields(EssayQuestion question, EssayQuestionDto questionDto) {
        question.setQuestionType(questionDto.getQuestionType());
        question.setName(questionDto.getName());
        question.setQuestionText(questionDto.getQuestionText());
        question.setGeneralFeedback(questionDto.getGeneralFeedback());
        question.setPenalty(questionDto.getPenalty());
        question.setHidden(questionDto.isHidden());
        question.setIdNumber(questionDto.getIdNumber());
        question.setDefaultGrade(questionDto.getDefaultGrade());
        question.setResponseFormat(questionDto.getResponseFormat());
        question.setResponseRequired(questionDto.isResponseRequired());
        question.setResponseFieldLines(questionDto.getResponseFieldLines());
        question.setMinWordLimit(questionDto.getMinWordLimit());
        question.setMaxWordLimit(questionDto.getMaxWordLimit());
        question.setAttachments(questionDto.getAttachments());
        question.setAttachmentsRequired(questionDto.getAttachmentsRequired());
        question.setMaxBytes(questionDto.getMaxBytes());
        question.setFileTypesList(questionDto.getFileTypesList());
        question.setGraderInfo(questionDto.getGraderInfo());
        question.setResponseTemplate(questionDto.getResponseTemplate());

        Category category = categoryRepository.findById(questionDto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        question.setCategory(category);

        List<Label> labels = labelRepository.findAllByIds(questionDto.getLabelIds());
        question.setLabels(labels);
    }

    @Override
    public Class<EssayQuestion> getQuestionType() {
        return EssayQuestion.class;
    }

    @Override
    public Class<EssayQuestionDto> getQuestionDtoType() {
        return EssayQuestionDto.class;
    }

    @Override
    public boolean isResponsibleFor(String type) {
        return "essay".equals(type);
    }

    @Override
    public Optional<EssayQuestion> saveFromXml(Element questionElement) {
        EssayQuestion question = new EssayQuestion();

        question.setQuestionType("EssayQuestion");
        question.setName(getTextContentByTagName(questionElement, "name"));
        question.setQuestionText(getTextContentByTagName(questionElement, "questiontext"));
        question.setGeneralFeedback(getTextContentByTagName(questionElement, "generalfeedback"));
        question.setPenalty(parseDouble(getTextContentByTagName(questionElement, "penalty")));
        question.setHidden(parseBoolean(getTextContentByTagName(questionElement, "hidden")));
        question.setIdNumber(getTextContentByTagName(questionElement, "idnumber"));

        List<String> fileTypesList = extractFileTypesList(questionElement);
        question.setFileTypesList(fileTypesList);
        question.setDefaultGrade(parseDouble(getTextContentByTagName(questionElement, "defaultgrade")));
        question.setResponseFormat(getTextContentByTagName(questionElement, "responseformat"));
        question.setResponseRequired(parseBoolean(getTextContentByTagName(questionElement, "responserequired")));
        question.setResponseFieldLines(parseInt(getTextContentByTagName(questionElement, "responsefieldlines")));
        question.setMinWordLimit(parseInt(getTextContentByTagName(questionElement, "minwordlimit")));
        question.setMaxWordLimit(parseInt(getTextContentByTagName(questionElement, "maxwordlimit")));
        question.setAttachments(parseInt(getTextContentByTagName(questionElement, "attachments")));
        question.setAttachmentsRequired(parseInt(getTextContentByTagName(questionElement, "attachmentsrequired")));
        question.setMaxBytes(parseLong(getTextContentByTagName(questionElement, "maxbytes")));
        question.setGraderInfo(getTextContentByTagName(questionElement, "graderinfo"));
        question.setResponseTemplate(getTextContentByTagName(questionElement, "responsetemplate"));

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

    private List<String> extractFileTypesList(Element questionElement) {
        NodeList fileTypeNodes = questionElement.getElementsByTagName("filetype");
        List<String> fileTypes = new ArrayList<>();
        for (int i = 0; i < fileTypeNodes.getLength(); i++) {
            Node fileTypeNode = fileTypeNodes.item(i);
            if (fileTypeNode.getNodeType() == Node.ELEMENT_NODE) {
                fileTypes.add(fileTypeNode.getTextContent());
            }
        }
        return fileTypes;
    }

    private double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean parseBoolean(String text) {
        return "1".equals(text);
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long parseLong(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return 0;
        }
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
    public Element toXmlElement(EssayQuestion question, Document doc) {
        // Create the root element for the question
        Element questionElement = doc.createElement("question");
        questionElement.setAttribute("type", "essay");

        // Add question name
        Element nameElement = doc.createElement("name");
        Element nameTextElement = doc.createElement("text");
        nameTextElement.appendChild(doc.createTextNode(question.getName()));
        nameElement.appendChild(nameTextElement);
        questionElement.appendChild(nameElement);

        // Add question text
        Element questionTextElement = doc.createElement("questiontext");
        questionTextElement.setAttribute("format", question.getResponseFormat());
        Element questionTextTextElement = doc.createElement("text");
        questionTextTextElement.appendChild(doc.createTextNode(question.getQuestionText()));
        questionTextElement.appendChild(questionTextTextElement);
        questionElement.appendChild(questionTextElement);

        // Add general feedback
        Element generalFeedbackElement = doc.createElement("generalfeedback");
        Element generalFeedbackTextElement = doc.createElement("text");
        generalFeedbackTextElement.appendChild(doc.createTextNode(question.getGeneralFeedback()));
        generalFeedbackElement.appendChild(generalFeedbackTextElement);
        questionElement.appendChild(generalFeedbackElement);

        // Optional: Add other fields like penalty, hidden, idNumber, defaultGrade, etc., similar to above.

        // Add category
        Element categoryElement = doc.createElement("category");
        Element categoryTextElement = doc.createElement("text");
        if (question.getCategory() != null) {
            categoryTextElement.appendChild(doc.createTextNode(question.getCategory().getName()));
        } else {
            categoryTextElement.appendChild(doc.createTextNode("Default Category"));
        }
        categoryElement.appendChild(categoryTextElement);
        questionElement.appendChild(categoryElement);

        // Optionally add labels as tags
        if (!question.getLabels().isEmpty()) {
            for (Label label : question.getLabels()) {
                Element tagElement = doc.createElement("tag");
                tagElement.appendChild(doc.createTextNode(label.getName()));
                questionElement.appendChild(tagElement);
            }
        }

        // Optionally, add custom fields specific to EssayQuestions like responseTemplate, fileTypesList, etc.

        return questionElement;
    }
}