package hackathon.bigone.sunsak.global.initData.mypage.qna;

import hackathon.bigone.sunsak.accounts.mypage.entity.Answer;
import hackathon.bigone.sunsak.accounts.mypage.entity.Question;
import hackathon.bigone.sunsak.accounts.mypage.repository.AnswerRepository;
import hackathon.bigone.sunsak.accounts.mypage.repository.QuestionRepository;
import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.accounts.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class QnaInit {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager em;

    @Bean
    @Order(7)
    public ApplicationRunner initQna() {
        return args -> {
            if (questionRepository.count() > 0) return; // 이미 있으면 스킵
            insertSampleQna();
        };
    }

    @Transactional
    protected void insertSampleQna() {
        // 0) 질문 작성자는 user_id = 1
        SiteUser qAuthor = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("user_id=1 유저가 없습니다."));

        // 1) 답변 작성자 admin 계정 없으면 생성
        SiteUser admin = userRepository.findByUsername("admin").orElseGet(() -> {
            SiteUser a = SiteUser.builder()
                    .username("admin")
                    .nickname("관리자")
                    .password("{noop}admin") // 실제 로그인 안쓸거면 noop로
                    .build();
            return userRepository.save(a);
        });

        // 2) Question 저장
        Question q = Question.builder()
                .title("제 계정이 해킹 당한 것 같습니다")
                .body("""
                        안녕하세요, 순삭 앱을 애용하고 있는 유저입니다.
                        제가 최근에 바빠서 앱 접속을 많이 못했는데, 오랜만에 들어와 보니까 제 닉네임이랑 게시물 내역이 모두 바뀌어 있더라구요... 일단 프로필은 다시 수정해 두었는데, 삭제된 게시물은 다시 복구할 수 없는 걸까요? ㅠㅠㅠ 답변 부탁드립니다.!!
                        """)
                .author(qAuthor)
                .build();
        q = questionRepository.save(q);

        // 3) Answer 저장
        Answer a = new Answer();
        a.setTitle("<제 계정이 해킹 당한 것 같습니다>에 대한 답변");
        a.setBody("""
                안녕하세요, 짜파게티 요리사님! 순삭 고객지원 담당자입니다. 우선, 고객님께 불편을 드려 죄송합니다.
                계정 해킹 건에 관해서는 마이페이지>신고 기능을 통해 신고해주시면, 이와 같은 일이 다시 발생하지 않도록 조치를 취하겠습니다.
                다만, 이미 삭제된 게시물에 대해서는 복구가 어렵다는 점 양해 부탁드립니다. 감사합니다.
                """);
        a.setAuthor(admin);
        a.setQuestion(q);
        a = answerRepository.save(a);

        // 4) 날짜 고정 (질문: 7/16, 답변: 7/18)
        setFixedTimestamps("question", qId(q), LocalDateTime.of(2025, 7, 16, 9, 0));
        setFixedTimestamps("answer", aId(a), LocalDateTime.of(2025, 7, 18, 9, 0));
    }

    private Long qId(Question q) {
        try {
            var f = Question.class.getSuperclass().getDeclaredField("id");
            f.setAccessible(true);
            return (Long) f.get(q);
        } catch (Exception e) {
            throw new IllegalStateException("Question ID 추출 실패", e);
        }
    }

    private Long aId(Answer a) {
        try {
            var f = Answer.class.getSuperclass().getDeclaredField("id");
            f.setAccessible(true);
            return (Long) f.get(a);
        } catch (Exception e) {
            throw new IllegalStateException("Answer ID 추출 실패", e);
        }
    }

    @Transactional
    protected void setFixedTimestamps(String table, Long id, LocalDateTime fixed) {
        em.createNativeQuery("UPDATE " + table + " SET created_date = ?, modified_date = ? WHERE id = ?")
                .setParameter(1, fixed)
                .setParameter(2, fixed)
                .setParameter(3, id)
                .executeUpdate();
    }
}
