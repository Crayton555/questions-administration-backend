package mk.ukim.finki.wpprojectexamquestionsadministration.service.implementation;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Question;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.QuestionDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.exceptions.QuestionNotFoundException;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.QuestionRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public List<Question> findAll() {
        return this.questionRepository.findAll();
    }

    @Override
    public Optional<Question> findById(Long id) {
        return this.questionRepository.findById(id);
    }

    @Override
    public Optional<Question> findByName(String name) {
        return this.questionRepository.findQuestionByName(name);
    }

    @Override
    @Transactional
    public Optional<Question> save(String name, String questionText, String generalFeedback, double penalty, boolean hidden, String idNumber) {
        return Optional.of(this.questionRepository.save(
                new Question(name, questionText, generalFeedback, penalty, hidden, idNumber)
        ));
    }

    @Override
    public Optional<Question> save(QuestionDto questionDto) {
        return Optional.of(this.questionRepository.save(new Question(
                questionDto.getName(),
                questionDto.getQuestionText(),
                questionDto.getGeneralFeedback(),
                questionDto.getPenalty(),
                questionDto.isHidden(),
                questionDto.getIdNumber()
        )));
    }

    @Override
    @Transactional
    public Optional<Question> edit(Long id, String name, String questionText, String generalFeedback, double penalty, boolean hidden, String idNumber) {
        Question question = this.questionRepository.findById(id).orElseThrow(() -> new QuestionNotFoundException(id));

        question.setName(name);
        question.setQuestionText(questionText);
        question.setGeneralFeedback(generalFeedback);
        question.setPenalty(penalty);
        question.setHidden(hidden);
        question.setIdNumber(idNumber);

        return Optional.of(this.questionRepository.save(question));
    }

    @Override
    public Optional<Question> edit(Long id, QuestionDto questionDto) {
        Question question = this.questionRepository.findById(id).orElseThrow(() -> new QuestionNotFoundException(id));

        question.setName(questionDto.getName());
        question.setQuestionText(questionDto.getQuestionText());
        question.setGeneralFeedback(questionDto.getGeneralFeedback());
        question.setPenalty(questionDto.getPenalty());
        question.setHidden(questionDto.isHidden());
        question.setIdNumber(questionDto.getIdNumber());

        return Optional.of(this.questionRepository.save(question));
    }

    @Override
    public void deleteById(Long id) {
        this.questionRepository.deleteById(id);
    }

    @Override
    public void saveQuestionsXML(InputStream xmlFile) {
        try {
            // Parse the XML file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            // Normalize the XML structure
            doc.getDocumentElement().normalize();

            // Get the list of "cloze" questions
            NodeList clozeQuestionList = doc.getElementsByTagName("question");

            // Iterate over the "cloze" questions and save them
            for (int i = 0; i < clozeQuestionList.getLength(); i++) {
                Element questionElement = (Element) clozeQuestionList.item(i);
                String questionType = questionElement.getAttribute("type");

                if (questionType.equalsIgnoreCase("cloze")) {
                    String questionName = questionElement.getElementsByTagName("name").item(0).getTextContent();
                    String questionText = questionElement.getElementsByTagName("questiontext").item(0).getTextContent();
                    String generalFeedback = questionElement.getElementsByTagName("generalfeedback").item(0).getTextContent();
                    double penalty = Double.parseDouble(questionElement.getElementsByTagName("penalty").item(0).getTextContent());
                    boolean hidden = Boolean.parseBoolean(questionElement.getElementsByTagName("hidden").item(0).getTextContent());
                    String idNumber = questionElement.getElementsByTagName("idnumber").item(0).getTextContent();

                    // Create and save the Question object
                    Question question = new Question(questionName, questionText, generalFeedback, penalty, hidden, idNumber);
                    questionRepository.save(question);
                }
            }

            System.out.println("Cloze questions saved successfully.");
        } catch (Exception e) {
            System.out.println("Error saving cloze questions: " + e.getMessage());
        }
    }
}
