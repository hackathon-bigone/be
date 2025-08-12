package hackathon.bigone.sunsak.accounts.mypage.repository;

import hackathon.bigone.sunsak.accounts.mypage.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer,Long> {
}
