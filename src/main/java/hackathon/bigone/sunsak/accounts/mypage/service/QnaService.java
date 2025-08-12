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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
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
    public QuestionDetailResponse createQuestion(Long userId, QuestionRequest req, List<MultipartFile> images)
            throws IOException {
        SiteUser author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<String> keys = new ArrayList<>();
        try {
            if (images != null) {
                for (MultipartFile f : images) {
                    if (f == null || f.isEmpty()) continue;
                    if (f.getContentType() == null || !f.getContentType().startsWith("image/")) {
                        throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
                    }

                    String key = s3Uploader.uploadOneUnder("qna/" + userId, f);
                    keys.add(key);
                }
            }

            Question question = Question
                    .builder()
                    .title(req.getTitle())
                    .body(req.getBody())
                    .imageUrls(keys)
                    .author(author)
                    .build();

            questionRepository.save(question);

            // 응답에는 presigned GET URL로 변환
            List<String> viewUrls = keys.stream()
                    .map(k -> s3Uploader.presignedGetUrl(k, Duration.ofMinutes(10)).toString())
                    .collect(Collectors.toList());

            QuestionDetailResponse res = QuestionDetailResponse.from(question);
            res.setImageUrls(viewUrls);

            return res;

        } catch (Exception e) {
            // DB 저장 실패 등 발생 시, 이미 업로드된 S3 객체 정리
            for (String key : keys) {
                try { s3Uploader.delete(key); } catch (Exception ignore) {}
            }
            throw e;
        }
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

        return new QuestionResponse(count,items);
    }

    //문의 상세 조회
    @Transactional(readOnly = true)
    public QuestionDetailResponse getMyQuestion(Long userId, Long questionId) {
        Question q = questionRepository.findByIdAndAuthorId(questionId, userId)
                .orElseThrow(() -> new NoSuchElementException("해당 문의를 찾을 수 없습니다."));

        return QuestionDetailResponse.from(q);
    }
}
