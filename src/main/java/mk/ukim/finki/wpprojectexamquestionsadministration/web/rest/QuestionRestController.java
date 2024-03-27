package mk.ukim.finki.wpprojectexamquestionsadministration.web.rest;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.LabelDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.questions.*;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;
import mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.service.IQuestionService;
import mk.ukim.finki.wpprojectexamquestionsadministration.service.questions.service.QuestionXmlExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequestMapping("/api/questions")
public class QuestionRestController {

    private final IQuestionService<BaseQuestion, Object> questionService;
    private final ObjectMapper objectMapper;
    private final QuestionXmlExportService xmlExportService;

    @Autowired
    public QuestionRestController(IQuestionService<BaseQuestion, Object> questionService, ObjectMapper objectMapper, QuestionXmlExportService xmlExportService) {
        this.questionService = questionService;
        this.objectMapper = objectMapper;
        this.xmlExportService = xmlExportService;
    }

    @GetMapping
    public ResponseEntity<List<BaseQuestion>> getAllQuestions() {
        List<BaseQuestion> questions = questionService.findAll();
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseQuestion> getQuestionById(@PathVariable Long id) {
        return questionService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<BaseQuestion> createQuestion(@RequestBody QuestionWrapperDto wrapper) {
        try {
            BaseQuestion question = null;
            switch (wrapper.getQuestionType().toLowerCase()) {
                case "categoryquestion":
                    question = questionService.save(objectMapper.treeToValue(wrapper.getQuestionData(), CategoryQuestionDto.class)).orElse(null);
                    break;
                case "clozequestion":
                    question = questionService.save(objectMapper.treeToValue(wrapper.getQuestionData(), ClozeQuestionDto.class)).orElse(null);
                    break;
                case "essayquestion":
                    question = questionService.save(objectMapper.treeToValue(wrapper.getQuestionData(), EssayQuestionDto.class)).orElse(null);
                    break;
                case "matchingquestion":
                    question = questionService.save(objectMapper.treeToValue(wrapper.getQuestionData(), MatchingQuestionDto.class)).orElse(null);
                    break;
                case "multichoicequestion":
                    question = questionService.save(objectMapper.treeToValue(wrapper.getQuestionData(), MultiChoiceQuestionDto.class)).orElse(null);
                    break;
                case "shortanswerquestion":
                    question = questionService.save(objectMapper.treeToValue(wrapper.getQuestionData(), ShortAnswerQuestionDto.class)).orElse(null);
                    break;
            }
            if (question != null) {
                return ResponseEntity.ok(question);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Error", "Error processing request: " + e.getMessage());
            return new ResponseEntity<>(null, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<BaseQuestion> updateQuestion(@PathVariable Long id, @RequestBody QuestionWrapperDto wrapper) {
        try {
            BaseQuestion question = null;
            switch (wrapper.getQuestionType().toLowerCase()) {
                case "categoryquestion":
                    question = questionService.edit(id, objectMapper.treeToValue(wrapper.getQuestionData(), CategoryQuestionDto.class)).orElse(null);
                    break;
                case "clozequestion":
                    question = questionService.edit(id, objectMapper.treeToValue(wrapper.getQuestionData(), ClozeQuestionDto.class)).orElse(null);
                    break;
                case "essayquestion":
                    question = questionService.edit(id, objectMapper.treeToValue(wrapper.getQuestionData(), EssayQuestionDto.class)).orElse(null);
                    break;
                case "matchingquestion":
                    question = questionService.edit(id, objectMapper.treeToValue(wrapper.getQuestionData(), MatchingQuestionDto.class)).orElse(null);
                    break;
                case "multichoicequestion":
                    question = questionService.edit(id, objectMapper.treeToValue(wrapper.getQuestionData(), MultiChoiceQuestionDto.class)).orElse(null);
                    break;
                case "shortanswerquestion":
                    question = questionService.edit(id, objectMapper.treeToValue(wrapper.getQuestionData(), ShortAnswerQuestionDto.class)).orElse(null);
                    break;
            }
            if (question != null) {
                return ResponseEntity.ok(question);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{questionId}/change-question-category/{newCategoryId}")
    public ResponseEntity<BaseQuestion> changeQuestionCategory(@PathVariable Long questionId, @PathVariable Long newCategoryId) {
        return questionService.changeQuestionCategory(questionId, newCategoryId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{questionId}/labels")
    public ResponseEntity<?> addNewLabelToQuestion(@PathVariable Long questionId, @RequestBody LabelDto labelDto) {
        try {
            questionService.addNewLabelToQuestion(questionId, labelDto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/upload-xml")
    public ResponseEntity<?> uploadQuestionsXml(@RequestParam("file") MultipartFile file) {
        try {
            InputStream xmlData = file.getInputStream();
            questionService.processQuestionsFromXml(xmlData);
            return ResponseEntity.ok("Successfully processed XML file.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process XML file: " + e.getMessage());
        }
    }

    @GetMapping("/export/xml")
    public ResponseEntity<String> exportQuestionsToXml() {
        try {
            Document xmlDocument = xmlExportService.exportQuestionsToXml();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(xmlDocument), new StreamResult(writer));
            String xmlString = writer.getBuffer().toString();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/xml");
            return ResponseEntity.ok().headers(headers).body(xmlString);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error exporting questions to XML: " + e.getMessage());
        }
    }
}