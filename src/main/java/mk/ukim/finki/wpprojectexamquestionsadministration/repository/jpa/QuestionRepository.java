package mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findQuestionByName(String name);
}

