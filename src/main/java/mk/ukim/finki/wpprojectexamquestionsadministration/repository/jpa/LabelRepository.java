package mk.ukim.finki.wpprojectexamquestionsadministration.repository.jpa;

import mk.ukim.finki.wpprojectexamquestionsadministration.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    @Query("SELECT l FROM Label l WHERE l.id IN :ids")
    List<Label> findAllByIds(@Param("ids") List<Long> ids);
    Optional<Label> findByName(String name);
}
