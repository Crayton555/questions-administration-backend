package mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.questions")
    List<Category> findAllWithQuestions();
    @Query("SELECT c FROM Category c WHERE c.id NOT IN :excludedIds")
    List<Category> findAllExcluding(@Param("excludedIds") Set<Long> excludedIds);
    @Query("SELECT c FROM Category c WHERE c.name = :name")
    Optional<Category> findByName(@Param("name") String name);
    @Query("SELECT c FROM Category c WHERE c.parentCategory IS NULL")
    List<Category> findRootCategories();
    @Query("SELECT c FROM Category c WHERE c.parentCategory.id = :parentId")
    List<Category> findByParentId(@Param("parentId") Long parentId);
}