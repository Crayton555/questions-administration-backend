package mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.strategy;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations.FormatType;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.CategoryRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.LabelRepository;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Field;
import java.util.*;

public interface QuestionStrategy<T extends BaseQuestion, D> {
    Optional<T> save(D questionDto);

    Optional<T> edit(Long id, D questionDto);

    void deleteById(Long id);

    Optional<T> findById(Long id);

    Class<T> getQuestionType();

    Class<D> getQuestionDtoType();

    boolean isResponsibleFor(String type);

    public Optional<T> saveFromXml(Element questionElement);

    @Transactional
    default void importBaseQuestionAttributes(Element questionElement, BaseQuestion question, CategoryRepository categoryRepository, LabelRepository labelRepository) {
        question.setName(getTextContentByTagName(questionElement, "name"));
        question.setQuestionText(getTextContentByTagName(questionElement, "questiontext"));
        question.setQuestionTextFormat(extractFormat(questionElement, "questiontext"));
        question.setGeneralFeedback(getTextContentByTagName(questionElement, "generalfeedback"));
        question.setGeneralFeedbackFormat(extractFormat(questionElement, "generalfeedback"));
        question.setPenalty(parseDouble(getTextContentByTagName(questionElement, "penalty")));
        question.setHidden(parseBoolean(getTextContentByTagName(questionElement, "hidden")));
        question.setIdNumber(getTextContentByTagName(questionElement, "idnumber"));

        NodeList tagsContainerList = questionElement.getElementsByTagName("tags");
        Map<String, Label> labelCache = new HashMap<>();
        if (tagsContainerList.getLength() > 0) {
            Node tagsContainerNode = tagsContainerList.item(0);
            if (tagsContainerNode.getNodeType() == Node.ELEMENT_NODE && tagsContainerNode.hasChildNodes()) {
                NodeList tagsList = ((Element) tagsContainerNode).getElementsByTagName("tag");
                List<Label> labels = new ArrayList<>();
                for (int i = 0; i < tagsList.getLength(); i++) {
                    Node tagNode = tagsList.item(i);
                    if (tagNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element tagElement = (Element) tagNode;
                        String tagText = tagElement.getTextContent().trim();
                        if (!tagText.isEmpty()) {
                            Label label = labelCache.computeIfAbsent(tagText, t -> {
                                Label foundLabel = labelRepository.findByName(t).orElse(null);
                                if (foundLabel == null) {
                                    foundLabel = labelRepository.save(new Label(t));
                                }
                                return foundLabel;
                            });
                            labels.add(label);
                        }
                    }
                }
                question.setLabels(labels);
            }
        }
    }

    default Element toXmlElement(T question, Document doc) {
        Element questionElement = doc.createElement("question");

        Element nameElement = doc.createElement("name");
        Element nameTextElement = doc.createElement("text");
        nameTextElement.appendChild(doc.createTextNode(question.getName()));
        nameElement.appendChild(nameTextElement);
        questionElement.appendChild(nameElement);

        Element questiontextElement = doc.createElement("questiontext");
        questiontextElement.setAttribute("format", question.getQuestionTextFormat().toString().toLowerCase());
        Element questionTextElement = doc.createElement("text");
        if (requiresCdata(question.getQuestionText())) {
            questionTextElement.appendChild(doc.createCDATASection(question.getQuestionText()));
        } else {
            questionTextElement.appendChild(doc.createTextNode(question.getQuestionText()));
        }
        questiontextElement.appendChild(questionTextElement);
        questionElement.appendChild(questiontextElement);

        Element generalFeedbackElement = doc.createElement("generalfeedback");
        generalFeedbackElement.setAttribute("format", question.getGeneralFeedbackFormat().toString().toLowerCase());
        Element feedbackTextElement = doc.createElement("text");

        if (question.getGeneralFeedback() != null && !question.getGeneralFeedback().isEmpty()) {
            if (requiresCdata(question.getGeneralFeedback())) {
                feedbackTextElement.appendChild(doc.createCDATASection(question.getGeneralFeedback()));
            } else {
                feedbackTextElement.appendChild(doc.createTextNode(question.getGeneralFeedback()));
            }
        }

        generalFeedbackElement.appendChild(feedbackTextElement);
        questionElement.appendChild(generalFeedbackElement);

        addOptionalElementByReflection(question, "defaultGrade", doc, questionElement);

        Element penaltyElement = doc.createElement("penalty");
        penaltyElement.appendChild(doc.createTextNode(String.valueOf(question.getPenalty())));
        questionElement.appendChild(penaltyElement);

        Element hiddenElement = doc.createElement("hidden");
        hiddenElement.appendChild(doc.createTextNode(question.isHidden() ? "1" : "0"));
        questionElement.appendChild(hiddenElement);

        Element idNumberElement = doc.createElement("idnumber");
        if (question.getIdNumber() != null && !question.getIdNumber().isEmpty()) {
            idNumberElement.appendChild(doc.createTextNode(question.getIdNumber()));
        }
        questionElement.appendChild(idNumberElement);

        if (question.getLabels() != null && !question.getLabels().isEmpty()) {
            Element tagsElement = doc.createElement("tags");
            for (Label label : question.getLabels()) {
                Element tagElement = doc.createElement("tag");
                Element textElement = doc.createElement("text");
                textElement.appendChild(doc.createTextNode(label.getName()));
                tagElement.appendChild(textElement);
                tagsElement.appendChild(tagElement);
            }
            questionElement.appendChild(tagsElement);
        }

        return questionElement;
    }

    default boolean requiresCdata(String text) {
        boolean needsCdata = text != null && (text.contains("<") || text.contains(">") || text.contains("&") || text.contains("\"") || text.contains("'"));
        System.out.println("Text requires CDATA: " + needsCdata + " for text: " + text);
        return needsCdata;
    }

    default String getTextContentByTagName(Element element, String tagName) {
        NodeList elements = element.getElementsByTagName(tagName);
        if (elements != null && elements.getLength() > 0) {
            Node firstNode = elements.item(0);
            if (firstNode != null && firstNode.hasChildNodes()) {
                NodeList childNodes = firstNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);
                    if ("text".equals(child.getNodeName())) {
                        return child.getTextContent().trim();
                    }
                }
            }
            return firstNode.getTextContent().trim();
        }
        return "";
    }

    default double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    default boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value.trim()) || "1".equals(value.trim());
    }

    default int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    default long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    default FormatType extractFormat(Element element, String elementName) {
        Element targetElement = element;
        if (!elementName.isEmpty()) {
            NodeList nodeList = element.getElementsByTagName(elementName);
            if (nodeList.getLength() > 0) {
                targetElement = (Element) nodeList.item(0);
            }
        }
        String format = targetElement.getAttribute("format");
        return switch (format) {
            case "html" -> FormatType.HTML;
            case "moodle_auto_format" -> FormatType.MOODLE_AUTO_FORMAT;
            case "plain_text" -> FormatType.PLAIN_TEXT;
            case "markdown" -> FormatType.MARKDOWN;
            default -> FormatType.HTML;
        };
    }

    private void addOptionalElementByReflection(T question, String fieldName, Document doc, Element parent) {
        try {
            Field field = question.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(question);
            if (value != null) {
                appendTextElement(doc, parent, fieldName.toLowerCase(), String.valueOf(value));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.out.println("Field '" + fieldName + "' not found or not accessible in " + question.getClass().getSimpleName());
        }
    }

    private void appendTextElement(Document doc, Element parent, String elementName, String textContent) {
        Element element = doc.createElement(elementName);
        element.appendChild(doc.createTextNode(textContent));
        parent.appendChild(element);
    }
}