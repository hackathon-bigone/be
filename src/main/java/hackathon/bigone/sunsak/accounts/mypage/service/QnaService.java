package hackathon.bigone.sunsak.accounts.mypage.service;

import hackathon.bigone.sunsak.accounts.mypage.dto.question.QuestionDetailResponse;
import hackathon.bigone.sunsak.accounts.mypage.dto.question.QuestionListResponse;
import hackathon.bigone.sunsak.accounts.mypage.dto.question.QuestionRequest;
import hackathon.bigone.sunsak.accounts.mypage.dto.question.QuestionResponse;
import hackathon.bigone.sunsak.accounts.mypage.entity.Question;
import hackathon.bigone.sunsak.accounts.mypage.repository.AnswerRepository;
import hackathon.bigone.sunsak.accounts.mypage.repository.QuestionRepository;
import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.accounts.user.repository.UserRepository;
import hackathon.bigone.sunsak.global.aws.s3.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QnaService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    //문의 작성
    @Transactional
    public QuestionDetailResponse createQuestion(Long userId, QuestionRequest req) {
        //작성자 조회
        SiteUser author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //S3 key 리스트 (이미 프론트가 Presigned PUT으로 업로드 완료한 key)
        List<String> keys = (req.getImageKeys() == null) ? List.of() : req.getImageKeys();

        Question question = Question.builder()
                .title(req.getTitle())
                .body(req.getBody())
                .author(author)
                .build();

        //이미지 중복 없이
        question.addImageKeys(keys);

        questionRepository.save(question);

        // 응답에는 presigned GET URL로 변환
        // - DB에는 key만 있지만, 프론트에서 바로 보기 위해 임시 접근 가능한 URL로 변환
        List<String> viewUrls = keys.stream()
                .map(k -> s3Uploader.presignedGetUrl(k, Duration.ofMinutes(30)).toString())
                .collect(Collectors.toList());

        QuestionDetailResponse res = QuestionDetailResponse.from(question);
        res.setImageUrls(viewUrls);

        return res;

    }

    //문의 전체 조회
    @Transactional(readOnly = true)
    public QuestionResponse getQuestions(Long userId) {
        List<QuestionListResponse> items = questionRepository
                .findByAuthorIdOrderByCreateDateDesc(userId)
                .stream()
                .map(QuestionListResponse::from)
                .toList();

        long count = items.size();
        String message = count == 0 ? "문의 내역이 없어요" : null;

        return new QuestionResponse(count,items, message);
    }

    //문의 상세 조회
    @Transactional(readOnly = true)
    public QuestionDetailResponse getMyQuestion(Long userId, Long questionId) {
        Question q = questionRepository.findByIdAndAuthorId(questionId, userId)
                .orElseThrow(() -> new NoSuchElementException("해당 문의를 찾을 수 없습니다."));

        List<String> viewUrls = q.getImageKeys().stream()
                .map(k -> s3Uploader.presignedGetUrl(k, Duration.ofMinutes(30)).toString())
                .toList();

        QuestionDetailResponse res = QuestionDetailResponse.from(q);
        res.setImageUrls(viewUrls);

        return res;
    }
}
