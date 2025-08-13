package hackathon.bigone.sunsak.accounts.mypage.controller;

import hackathon.bigone.sunsak.accounts.mypage.dto.NoticeDto;
import hackathon.bigone.sunsak.accounts.mypage.dto.PasswordChangeDto;
import hackathon.bigone.sunsak.accounts.mypage.dto.question.QuestionDetailResponse;
import hackathon.bigone.sunsak.accounts.mypage.dto.question.QuestionRequest;
import hackathon.bigone.sunsak.accounts.mypage.dto.question.QuestionResponse;
import hackathon.bigone.sunsak.accounts.mypage.repository.QuestionRepository;
import hackathon.bigone.sunsak.accounts.mypage.service.MypageService;
import hackathon.bigone.sunsak.accounts.mypage.service.QnaService;
import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.global.security.jwt.CustomUserDetail;
import hackathon.bigone.sunsak.global.validate.accounts.SignupValidator;
import hackathon.bigone.sunsak.recipe.board.dto.BoardDto;
import hackathon.bigone.sunsak.recipe.board.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {
    private final SignupValidator signupValidator;
    private final MypageService mypageService;
    private final BoardService boardService; //레시피
    private final QnaService qnaService;
    private final QuestionRepository questionRepository;

    //마이페이지 조회
    @GetMapping("")
    public ResponseEntity<?> getMypage(Authentication authentication){
        if(authentication==null){
            return ResponseEntity.ok(
                    Map.of("message", "로그인 하고 순삭의 다양한 서비스를 경험해보세요!")
            );
        }
        CustomUserDetail user = (CustomUserDetail) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of(
                "nickname", user.getNickname(),
                "username", user.getUsername()
        ));
   }

   //닉네임 수정
   @PatchMapping("/nickname")
   public ResponseEntity<?> updateNickname(
           @AuthenticationPrincipal CustomUserDetail userDetail,
           @RequestBody Map<String, String> request
   ){
       if(userDetail== null){
           return ResponseEntity.ok(
                   Map.of("message", "로그인을 해주세요")
           );
       }

       String nickname = request.get("nickname");
       signupValidator.nicknameValidate(nickname);

       mypageService.updateNickname(userDetail.getId(), nickname);
       return ResponseEntity.ok(Map.of(
               "message", "닉네임 변경이 완료되었습니다."
       ));
   }

   //비밀번호 수정
   @PatchMapping("/password")
   public ResponseEntity<?> updatePassword(
           @AuthenticationPrincipal CustomUserDetail userDetail,
           @RequestBody PasswordChangeDto req
   ){
        if(userDetail== null){
            return ResponseEntity.ok(
                    Map.of("message", "로그인을 해주세요")
            );
        }
        signupValidator.passwordValidate(req.getNewPassword(), req.getRepeatPw());
        mypageService.updatePassword(userDetail.getId(), req);
        return ResponseEntity.ok(Map.of(
               "message", "비밀번호 변경이 완료되었습니다."
        ));
   }

   //공지사항
   @GetMapping("/notice") // 전체 조회
   public ResponseEntity<List<NoticeDto>> getAllNotices(Authentication authentication){
       if (authentication == null || !authentication.isAuthenticated()) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
       }

       List<NoticeDto> notices = mypageService.getAllNotices();
       return ResponseEntity.ok(notices);
   }

   //상세 조회
    @GetMapping("/notice/{noticeId}")
    public ResponseEntity<NoticeDto> getNotice(
            Authentication authentication,
            @PathVariable Long noticeId
    ){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return mypageService.getNoticeById(noticeId)
                .map(ResponseEntity::ok)                // 200 + DTO(JSON)
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404
    }

    //스크랩
    @GetMapping("/scrap")
    public ResponseEntity<List<BoardDto>> getMyScrapBoards(@AuthenticationPrincipal CustomUserDetail userDetail){
        if(userDetail == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        SiteUser currentUser = userDetail.getUser();

        List<BoardDto> scrapBoards = boardService.getScrapBoardsByUser(currentUser);
        return ResponseEntity.ok(scrapBoards);
    }

    //전체 조회
    @GetMapping("/qna")
    public ResponseEntity<QuestionResponse> getQuestions(
            @AuthenticationPrincipal CustomUserDetail userDetail
    ){
        if(userDetail==null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Long userId = userDetail.getId();

        return ResponseEntity.ok(qnaService.getQuestions(userId));
    }

    //상세 조회
    @GetMapping("/qna/{questionId}")
    public ResponseEntity<QuestionDetailResponse> getMyQuestion(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable Long questionId
    ){
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(qnaService.getMyQuestion(userDetail.getId(), questionId));
    }

    //qna 작성, json으로 프론트에서 온 key(url)만 String으로 저장하면 됨
    @PostMapping(value = "/qna", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuestionDetailResponse> createQuestion(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Valid @RequestBody QuestionRequest req
    ){
        if(userDetail == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Long userId = userDetail.getId();
        return ResponseEntity.ok(qnaService.createQuestion(userId, req));
    }
}
