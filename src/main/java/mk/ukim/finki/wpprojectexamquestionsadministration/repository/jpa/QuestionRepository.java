package mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Category;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<BaseQuestion, Long> {
    @Query("SELECT q FROM BaseQuestion q WHERE q.id IN :ids")
    List<BaseQuestion> findAllByIds(@Param("ids") List<Long> ids);
    @Query("SELECT q FROM BaseQuestion q WHERE q.category = :category")
    List<BaseQuestion> findByCategory(@Param("category") Category category);
}