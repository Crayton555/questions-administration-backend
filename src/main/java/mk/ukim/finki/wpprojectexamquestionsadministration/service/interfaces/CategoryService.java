package mk.ukim.finki.wpprojectexamquestionsadministration.service.interfaces;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.CategoryDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CategoryService {
    List<Category> findAll();

    Optional<Category> findById(Long id);

    Optional<Category> save(CategoryDto categoryDto);

    Optional<Category> edit(Long id, CategoryDto categoryDto);

    void deleteById(Long id);
    List<BaseQuestion> getAllQuestionsByCategory(Long categoryId);
    List<Category> findAllCategoriesWithQuestions();
    Set<Long> gatherDescendantCategoryIds(Long categoryId);

    void collectDescendantIds(Category category, Set<Long> descendantIds);
    List<Category> findAllExcluding(Set<Long> excludedIds);
}
