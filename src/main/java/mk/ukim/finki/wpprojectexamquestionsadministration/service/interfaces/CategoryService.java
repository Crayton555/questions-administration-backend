package mk.ukim.finki.wpprojectexamquestionsadministration.service.interfaces;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.CategoryDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> findAll();

    Optional<Category> findById(Long id);

    Optional<Category> save(String name, List<Long> questionIds);

    Optional<Category> save(CategoryDto categoryDto);

    Optional<Category> edit(Long id, String name, List<Long> questionIds);

    Optional<Category> edit(Long id, CategoryDto categoryDto);

    void deleteById(Long id);
    List<BaseQuestion> getAllQuestionsByCategory(Long categoryId);
    List<Category> findAllCategoriesWithQuestions();
}
