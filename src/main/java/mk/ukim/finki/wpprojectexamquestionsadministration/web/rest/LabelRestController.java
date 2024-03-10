package mk.ukim.finki.wpprojectexamquestionsadministration.web.rest;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.LabelDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.service.interfaces.LabelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequestMapping("/api/labels")
public class LabelRestController {

    private final LabelService labelService;

    public LabelRestController(LabelService labelService) {
        this.labelService = labelService;
    }

    @GetMapping
    public List<Label> getAllLabels() {
        return labelService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Label> getLabelById(@PathVariable Long id) {
        return this.labelService.findById(id)
                .map(label -> ResponseEntity.ok().body(label))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<Label> createLabel(@RequestBody LabelDto labelDto) {
        return this.labelService.save(labelDto)
                .map(label -> ResponseEntity.ok().body(label))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<Label> updateLabel(@PathVariable Long id, @RequestBody LabelDto labelDto) {
        return this.labelService.edit(id, labelDto)
                .map(label -> ResponseEntity.ok().body(label))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long id) {
        this.labelService.deleteById(id);
        if (this.labelService.findById(id).isEmpty()) return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().build();
    }
}
