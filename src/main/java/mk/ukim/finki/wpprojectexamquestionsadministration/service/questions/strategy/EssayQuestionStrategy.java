package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.ClozeQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.EssayQuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations.FormatType;
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

        question.setQuestionTextFormat(questionDto.getQuestionTextFormat());
        question.setGeneralFeedbackFormat(questionDto.getGeneralFeedbackFormat());
        question.setGraderInfoFormat(questionDto.getGraderInfoFormat());
        question.setResponseTemplateFormat(questionDto.getResponseTemplateFormat());

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

        importBaseQuestionAttributes(questionElement, question, categoryRepository, labelRepository);

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
        question.setGraderInfoFormat(extractFormat(questionElement, "graderinfo"));
        question.setResponseTemplate(getTextContentByTagName(questionElement, "responsetemplate"));
        question.setResponseTemplateFormat(extractFormat(questionElement, "responsetemplate"));

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

    @Override
    public Element toXmlElement(EssayQuestion question, Document doc) {
        Element questionElement = QuestionStrategy.super.toXmlElement(question, doc);
        questionElement.setAttribute("type", "essay");

        addSimpleElement(questionElement, doc, "responseformat", question.getResponseFormat());
        addSimpleElement(questionElement, doc, "responserequired", question.isResponseRequired() ? "1" : "0");
        addSimpleElement(questionElement, doc, "responsefieldlines", String.valueOf(question.getResponseFieldLines()));
        addSimpleElement(questionElement, doc, "attachments", String.valueOf(question.getAttachments()));
        addSimpleElement(questionElement, doc, "attachmentsrequired", String.valueOf(question.getAttachmentsRequired()));
        addSimpleElement(questionElement, doc, "maxbytes", String.valueOf(question.getMaxBytes()));

        if (question.getGraderInfo() != null && !question.getGraderInfo().isEmpty()) {
            Element graderInfoElement = doc.createElement("graderinfo");
            graderInfoElement.setAttribute("format", question.getGraderInfoFormat().toString().toLowerCase());
            Element graderInfoContent = doc.createElement("text");
            graderInfoContent.appendChild(doc.createTextNode(question.getGraderInfo()));
            graderInfoElement.appendChild(graderInfoContent);
            questionElement.appendChild(graderInfoElement);
        }

        if (question.getResponseTemplate() != null && !question.getResponseTemplate().isEmpty()) {
            Element responseTemplateElement = doc.createElement("responsetemplate");
            responseTemplateElement.setAttribute("format", question.getResponseTemplateFormat().toString().toLowerCase());
            Element responseTemplateContent = doc.createElement("text");
            responseTemplateContent.appendChild(doc.createTextNode(question.getResponseTemplate()));
            responseTemplateElement.appendChild(responseTemplateContent);
            questionElement.appendChild(responseTemplateElement);
        }

        return questionElement;
    }

    private void addSimpleElement(Element parent, Document doc, String tagName, String textContent) {
        if (textContent != null && !textContent.isEmpty()) {
            Element element = doc.createElement(tagName);
            element.appendChild(doc.createTextNode(textContent));
            parent.appendChild(element);
        }
    }
}