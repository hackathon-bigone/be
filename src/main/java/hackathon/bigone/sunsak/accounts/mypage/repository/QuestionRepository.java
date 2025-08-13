package hackathon.bigone.sunsak.accounts.mypage.repository;

import hackathon.bigone.sunsak.accounts.mypage.entity.Question;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    @EntityGraph(attributePaths = "answer")
    List<Question> findByAuthorIdOrderByCreateDateDesc(Long userId);

    Optional<Question> findByIdAndAuthorId(Long questionId, Long userId);

    @EntityGraph(attributePaths = "answer")
    Optional<Question> findById(Long id);
}
