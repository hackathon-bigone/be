package hackathon.bigone.sunsak.accounts.mypage.repository;

import hackathon.bigone.sunsak.accounts.mypage.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByAuthorIdOrderByCreateDateDesc(Long userId);
}
