//package mk.ukim.finki.wpprojectexamquestionsadministration.web.controller;
//
//import mk.ukim.finki.wpprojectexamquestionsadministration.model.Question;
//import mk.ukim.finki.wpprojectexamquestionsadministration.service.QuestionService;
//import org.springframework.http.MediaType;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//import java.io.OutputStream;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//
//import static org.thymeleaf.util.StringUtils.escapeXml;
//
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Controller
//@RequestMapping("/questions")
//public class QuestionController {
//    private final QuestionService questionService;
//
//    public QuestionController(QuestionService questionService) {
//        this.questionService = questionService;
//    }
//
//    @GetMapping
//    public String getQuestionPage(@RequestParam(required = false) String error, Model model) {
//        if (error != null && !error.isEmpty()) {
//            model.addAttribute("hasError", true);
//            model.addAttribute("error", error);
//        }
//        List<Question> questions = this.questionService.findAll();
//        model.addAttribute("questions", questions);
//        model.addAttribute("bodyContent", "questions");
//        return "master-template";
//    }
//
//    @DeleteMapping("/delete/{id}")
//    public String deleteQuestion(@PathVariable Long id) {
//        this.questionService.deleteById(id);
//        return "redirect:/questions";
//    }
//
//    @GetMapping("/edit-form/{id}")
//    public String editQuestionPage(@PathVariable Long id, Model model) {
//        if (this.questionService.findById(id).isPresent()) {
//            Question question = this.questionService.findById(id).get();
//            model.addAttribute("question", question);
//            model.addAttribute("bodyContent", "add-question");
//            return "master-template";
//        }
//        return "redirect:/questions?error=QuestionNotFound";
//    }
//
//    @GetMapping("/add-form")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public String addQuestionPage(Model model) {
//        model.addAttribute("bodyContent", "add-question");
//        return "master-template";
//    }
//
//    @PostMapping("/add")
//    public String saveQuestion(
//            @RequestParam(required = false) Long id,
//            @RequestParam String name,
//            @RequestParam String questionText,
//            @RequestParam String generalFeedback,
//            @RequestParam String penalty,
//            @RequestParam String hidden,
//            @RequestParam String idNumber) {
//        if (id != null) {
//            this.questionService.edit(
//                    id,
//                    name,
//                    questionText,
//                    generalFeedback,
//                    Double.parseDouble(penalty),
//                    Boolean.parseBoolean(hidden),
//                    idNumber);
//        } else {
//            this.questionService.save(
//                    name,
//                    questionText,
//                    generalFeedback,
//                    Double.parseDouble(penalty),
//                    Boolean.parseBoolean(hidden),
//                    idNumber);
//        }
//        return "redirect:/questions";
//    }
//
//    @GetMapping("/uploadXML")
//    public String uploadXMLFile() {
//        return "uploadXML";
//    }
//
//    @PostMapping("/uploadXML")
//    public String uploadXMLFile(@RequestParam("file") MultipartFile file) {
//        if (file.isEmpty()) {
//            // Handle empty file error
//            return "redirect:/questions";
//        }
//
//        try {
//            // Call the questionService to save the questions from the XML file
//            questionService.saveQuestionsXML(file.getInputStream());
//            return "redirect:/questions"; // Redirect to success page
//        } catch (Exception e) {
//            // Handle file processing error
//            return "redirect:/questions";
//        }
//    }
//
//    @GetMapping(value = "/exportXMLFile", produces = MediaType.APPLICATION_XML_VALUE)
//    public void exportXMLFile(HttpServletResponse response) throws IOException {
//        List<Question> questions = questionService.findAll();
//
//        response.setHeader("Content-Disposition", "attachment; filename=questions.xml");
//        response.setContentType(MediaType.APPLICATION_XML_VALUE);
//        response.setCharacterEncoding("UTF-8");
//
//        String xmlContent = generateXmlContent(questions);
//
//        try (OutputStream outputStream = response.getOutputStream()) {
//            byte[] contentBytes = xmlContent.getBytes(StandardCharsets.UTF_8);
//            outputStream.write(contentBytes);
//            outputStream.flush();
//        }
//    }
//
//    private String generateXmlContent(List<Question> questions) {
//        StringBuilder xmlBuilder = new StringBuilder();
//        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
//        xmlBuilder.append("<questions>\n");
//
//        for (Question question : questions) {
//            xmlBuilder.append("  <question type=\"cloze\">\n");
//            xmlBuilder.append("    <name>\n");
//            xmlBuilder.append("      <text>").append(escapeXml(question.getName())).append("</text>\n");
//            xmlBuilder.append("    </name>\n");
//            xmlBuilder.append("    <questiontext format=\"html\">\n");
//            xmlBuilder.append("      <text><![CDATA[").append(question.getQuestionText()).append("]]></text>\n");
//            xmlBuilder.append("    </questiontext>\n");
//            xmlBuilder.append("    <generalfeedback format=\"html\">\n");
//            xmlBuilder.append("      <text></text>\n");
//            xmlBuilder.append("    </generalfeedback>\n");
//            xmlBuilder.append("    <penalty>").append(question.getPenalty()).append("</penalty>\n");
//            xmlBuilder.append("    <hidden>").append(question.isHidden()).append("</hidden>\n");
//            xmlBuilder.append("    <idnumber></idnumber>\n");
//            xmlBuilder.append("  </question>\n");
//        }
//
//        xmlBuilder.append("</questions>");
//
//        return xmlBuilder.toString();
//    }
//
//
//}
