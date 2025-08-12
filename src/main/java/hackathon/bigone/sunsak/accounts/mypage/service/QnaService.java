package hackathon.bigone.sunsak.accounts.mypage.service;

import hackathon.bigone.sunsak.accounts.mypage.dto.QuestionRequest;
import hackathon.bigone.sunsak.accounts.mypage.dto.QuestionResponse;
import hackathon.bigone.sunsak.accounts.mypage.entity.Question;
import hackathon.bigone.sunsak.accounts.mypage.repository.AnswerRepository;
import hackathon.bigone.sunsak.accounts.mypage.repository.QuestionRepository;
import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.accounts.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    @Transactional
    public QuestionResponse createQuestion(Long userId, QuestionRequest req) {
        SiteUser author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Question question = Question
                .builder()
                .title(req.getTitle())
                .body(req.getBody())
                .imageUrls(req.getImageUrls())
                .author(author)
                .build();

        questionRepository.save(question);

        return QuestionResponse.from(question);
    }

    @Transactional
    public List<QuestionResponse> getMyQuestions(Long userId) {
        List<Question> questions = questionRepository.findByAuthorIdOrderByCreateDateDesc(userId);

        return questions.stream()
                .map(QuestionResponse::from)
                .toList();
    }
}
