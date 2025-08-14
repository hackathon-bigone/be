package hackathon.bigone.sunsak.global.initData.mypage.qna;

import hackathon.bigone.sunsak.accounts.mypage.entity.Answer;
import hackathon.bigone.sunsak.accounts.mypage.entity.Question;
import hackathon.bigone.sunsak.accounts.mypage.repository.AnswerRepository;
import hackathon.bigone.sunsak.accounts.mypage.repository.QuestionRepository;
import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.accounts.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AnswerInit {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    private static final long TARGET_QUESTION_ID = 1L;
    private static final long AUTHOR_USER_ID = 5L;

    @Bean
    @Order(11) // Question 초기화 이후
    public ApplicationRunner initAnswerForQuestion1() {
        return args -> insertDefaultAnswerIfNeeded();
    }

    /* --- Helpers --- */

    private void insertDefaultAnswerIfNeeded() {
        Question q = findQuestionOrLog(TARGET_QUESTION_ID);
        if (q == null) return;

        if (hasAnswer(q.getId())) {
            log.info("[AnswerInit] Skip: question {} already answered", q.getId());
            return;
        }

        SiteUser author = findUserOrThrow(AUTHOR_USER_ID);
        Answer answer = createAnswer(author, q);

        // 양방향 연관관계 편의 메서드 사용 (Answer.setQuestion)
        answer.setQuestion(q);
        answerRepository.save(answer);

        log.info("[AnswerInit] Inserted default answer for question {}", q.getId());
    }

    private Question findQuestionOrLog(Long id) {
        return questionRepository.findById(id).orElseGet(() -> {
            log.warn("[AnswerInit] Skip: question {} not found", id);
            return null;
        });
    }

    private boolean hasAnswer(Long questionId) {
        try {
            // 프로젝트 레포지토리에 맞춰 선택
            return answerRepository.existsByQuestionId(questionId);
        } catch (Exception e) {
            return answerRepository.findByQuestionId(questionId).isPresent();
        }
    }

    private SiteUser findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Author user " + userId + " not found"));
    }

    private Answer createAnswer(SiteUser author, Question question) {
        Answer a = new Answer();
        a.setTitle("<제 계정이 해킹 당한 것 같습니다>에 대한 답변");
        a.setBody(
                "안녕하세요, 짜파게티 요리사님! 순삭 고객지원 담당자입니다. 우선, 고객님께 불편을 드려 죄송합니다. " +
                        "계정 해킹 건에 관해서는 마이페이지>신고 기능을 통해 신고해주시면, 이와 같은 일이 다시 발생하지 않도록 조치를 취하겠습니다. " +
                        "다만, 이미 삭제된 게시물에 대해서는 복구가 어렵다는 점 양해 부탁드립니다. 감사합니다."
        );
        a.setAuthor(author);
        // a.setQuestion(question); // save 직전에 setQuestion에서 처리
        return a;
    }
}
