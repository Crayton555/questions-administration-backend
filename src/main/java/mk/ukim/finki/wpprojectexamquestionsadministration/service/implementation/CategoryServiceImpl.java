package mk.ukim.finki.wpprojectexamquestionsadministration.service.implementation;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.CategoryDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.CategoryRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.QuestionRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.service.interfaces.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, QuestionRepository questionRepository) {
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public List<Category> findAll() {
        return this.categoryRepository.findAll();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return this.categoryRepository.findById(id);
    }

    @Override
    @Transactional
    public Optional<Category> save(String name, List<Long> questionIds) {
        List<BaseQuestion> questions = this.questionRepository.findAllByIds(questionIds);

        Category category = new Category(name, questions);
        this.categoryRepository.save(category);

        return Optional.of(category);
    }

    @Override
    public Optional<Category> save(CategoryDto categoryDto) {
        List<BaseQuestion> questions = this.questionRepository.findAllByIds(categoryDto.getQuestionIds());

        Category category = new Category(categoryDto.getName(), questions);
        this.categoryRepository.save(category);

        return Optional.of(category);
    }

    @Override
    @Transactional
    public Optional<Category> edit(Long id, String name, List<Long> questionIds) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(name);
        this.categoryRepository.save(category);

        return Optional.of(category);
    }

    @Override
    public Optional<Category> edit(Long id, CategoryDto categoryDto) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(categoryDto.getName());
        this.categoryRepository.save(category);

        return Optional.of(category);
    }

    @Override
    public void deleteById(Long id) {
        Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        List<BaseQuestion> questions = category.getQuestions();
        if (questions != null) {
            for (BaseQuestion question : questions) {
                question.setCategory(null);
                this.questionRepository.save(question);
            }
        }

        this.categoryRepository.delete(category);
    }

    @Override
    public List<BaseQuestion> getAllQuestionsByCategory(Long categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        return category.map(Category::getQuestions).orElse(null);
    }

    @Override
    public List<Category> findAllCategoriesWithQuestions() {
        return categoryRepository.findAllWithQuestions();
    }
}