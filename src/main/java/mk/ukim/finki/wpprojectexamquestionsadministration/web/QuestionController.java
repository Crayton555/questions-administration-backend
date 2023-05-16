package mk.ukim.finki.wpprojectexamquestionsadministration.web;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Question;
import mk.ukim.finki.wpprojectexamquestionsadministration.service.QuestionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Controller
@RequestMapping("/questions")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public String getQuestionPage(@RequestParam(required = false) String error, Model model) {
        if (error != null && !error.isEmpty()) {
            model.addAttribute("hasError", true);
            model.addAttribute("error", error);
        }
        List<Question> questions = this.questionService.findAll();
        model.addAttribute("questions", questions);
        model.addAttribute("bodyContent", "questions");
        return "master-template";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteQuestion(@PathVariable Long id) {
        this.questionService.deleteById(id);
        return "redirect:/questions";
    }

    @GetMapping("/edit-form/{id}")
    public String editQuestionPage(@PathVariable Long id, Model model) {
        if (this.questionService.findById(id).isPresent()) {
            Question question = this.questionService.findById(id).get();
            model.addAttribute("question", question);
            model.addAttribute("bodyContent", "add-question");
            return "master-template";
        }
        return "redirect:/questions?error=QuestionNotFound";
    }

    @GetMapping("/add-form")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String addQuestionPage(Model model) {
        model.addAttribute("bodyContent", "add-question");
        return "master-template";
    }

    @PostMapping("/add")
    public String saveQuestion(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam String questionText,
            @RequestParam String generalFeedback,
            @RequestParam String penalty,
            @RequestParam String hidden,
            @RequestParam String idNumber) {
        if (id != null) {
            this.questionService.edit(
                    id,
                    name,
                    questionText,
                    generalFeedback,
                    Double.parseDouble(penalty),
                    Boolean.parseBoolean(hidden),
                    idNumber);
        } else {
            this.questionService.save(
                    name,
                    questionText,
                    generalFeedback,
                    Double.parseDouble(penalty),
                    Boolean.parseBoolean(hidden),
                    idNumber);
        }
        return "redirect:/questions";
    }

    @GetMapping("/uploadXML")
    public String uploadXMLFile() {
        return "uploadXML";
    }

    @PostMapping("/uploadXML")
    public String uploadXMLFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            // Handle empty file error
            return "redirect:/questions";
        }

        try {
            // Call the questionService to save the questions from the XML file
            questionService.saveQuestionsXML(file.getInputStream());
            return "redirect:/questions"; // Redirect to success page
        } catch (Exception e) {
            // Handle file processing error
            return "redirect:/questions";
        }
    }
}
