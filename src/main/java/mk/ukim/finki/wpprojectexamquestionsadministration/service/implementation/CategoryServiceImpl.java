package mk.ukim.finki.wpprojectexamquestionsadministration.service.implementation;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.dto.CategoryDto;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations.FormatType;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.CategoryRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa.QuestionRepository;
import mk.ukim.finki.wpprojectexamquestionsadministration.service.interfaces.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    public Optional<Category> save(CategoryDto categoryDto) {
        Category category = new Category(categoryDto.getName(), categoryDto.getInfo(), FormatType.valueOf(categoryDto.getInfoTextFormat()), categoryDto.getIdNumber());
        List<BaseQuestion> questions = this.questionRepository.findAllByIds(categoryDto.getQuestionIds());
        category.setQuestions(questions);
        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDto.getParentId()).orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
            category.setParentCategory(parent);
        }
        return Optional.of(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public Optional<Category> edit(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Category not found"));
        category.setName(categoryDto.getName());
        category.setInfo(categoryDto.getInfo());
        category.setInfoTextFormat(FormatType.valueOf(categoryDto.getInfoTextFormat()));
        category.setIdNumber(categoryDto.getIdNumber());
        List<BaseQuestion> updatedQuestions = questionRepository.findAllByIds(categoryDto.getQuestionIds());
        category.setQuestions(updatedQuestions);
        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDto.getParentId()).orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }
        return Optional.of(categoryRepository.save(category));
    }

    @Override
    public void deleteById(Long id) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Category not found"));

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

    @Override
    public Set<Long> gatherDescendantCategoryIds(Long categoryId) {
        Set<Long> descendantIds = new HashSet<>();
        Optional<Category> category = categoryRepository.findById(categoryId);
        if (category.isPresent()) {
            collectDescendantIds(category.get(), descendantIds);
        }
        return descendantIds;
    }

    @Override
    public void collectDescendantIds(Category category, Set<Long> descendantIds) {
        List<Category> subcategories = category.getSubcategories();
        for (Category subcategory : subcategories) {
            descendantIds.add(subcategory.getId());
            collectDescendantIds(subcategory, descendantIds);
        }
    }

    @Override
    public List<Category> findAllExcluding(Set<Long> excludedIds) {
        System.out.println("Excluded IDs: " + excludedIds);
        List<Category> categories = categoryRepository.findAllExcluding(excludedIds);
        System.out.println("Returned categories: " + categories);
        return categories;
    }
}