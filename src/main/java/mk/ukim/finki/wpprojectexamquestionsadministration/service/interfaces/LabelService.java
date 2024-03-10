package mk.ukim.finki.wpprojectexamquestionsadministration.service.interfaces;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.LabelDto;

import java.util.List;
import java.util.Optional;

public interface LabelService {
    List<Label> findAll();

    Optional<Label> findById(Long id);

    Optional<Label> save(String name, List<Long> questionIds);

    Optional<Label> save(LabelDto labelDto);

    Optional<Label> edit(Long id, String name, List<Long> questionIds);

    Optional<Label> edit(Long id, LabelDto labelDto);

    void deleteById(Long id);
}
