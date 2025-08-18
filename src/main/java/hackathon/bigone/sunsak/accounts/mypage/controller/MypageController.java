package hackathon.bigone.sunsak.accounts.mypage.controller;

import hackathon.bigone.sunsak.accounts.mypage.dto.NoticeDto;
import hackathon.bigone.sunsak.accounts.mypage.dto.PasswordChangeDto;
import hackathon.bigone.sunsak.accounts.mypage.dto.ReportDto;
import hackathon.bigone.sunsak.accounts.mypage.dto.question.QuestionDetailResponse;
import hackathon.bigone.sunsak.accounts.mypage.dto.question.QuestionRequest;
import hackathon.bigone.sunsak.accounts.mypage.dto.question.QuestionResponse;
import hackathon.bigone.sunsak.accounts.mypage.service.MypageService;
import hackathon.bigone.sunsak.accounts.mypage.service.QnaService;
import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.accounts.user.service.SignupService;
import hackathon.bigone.sunsak.global.security.jwt.CustomUserDetail;
import hackathon.bigone.sunsak.global.validate.accounts.SignupValidator;
import hackathon.bigone.sunsak.groupbuy.board.dto.GroupbuyResponseDto;
import hackathon.bigone.sunsak.groupbuy.board.service.GroupBuyService;
import hackathon.bigone.sunsak.groupbuy.comment.dto.MyGroupBuyCommentDto;
import hackathon.bigone.sunsak.groupbuy.comment.service.GroupBuyCommentService;
import hackathon.bigone.sunsak.recipe.board.dto.BoardResponseDto;
import hackathon.bigone.sunsak.recipe.board.service.BoardService;
import hackathon.bigone.sunsak.recipe.comment.dto.MypageDto;
import hackathon.bigone.sunsak.recipe.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {
    private final SignupValidator signupValidator;
    private final MypageService mypageService;
    private final BoardService boardService; //레시피
    private final GroupBuyService groupBuyService;
    private final QnaService qnaService;
    private final SignupService signupService;
    private final CommentService recipeCommentService;
    private final GroupBuyCommentService groupBuyCommentService;


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
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //레시피스크랩
    @GetMapping("/recipe-scrap")
    public ResponseEntity<List<BoardResponseDto>> getMyScrapBoards(@AuthenticationPrincipal CustomUserDetail userDetail){
        if(userDetail == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        SiteUser user = userDetail.getUser();

        List<BoardResponseDto> scrapBoards = boardService.getScrapBoardsByUser(user);
        return ResponseEntity.ok(scrapBoards);
    }

    @GetMapping("/groupbuy-scrap")
    public ResponseEntity<List<GroupbuyResponseDto>> getMyScrapGroupbuys(@AuthenticationPrincipal CustomUserDetail userDetail){
        if(userDetail == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        SiteUser user = userDetail.getUser();

        List<GroupbuyResponseDto> scrapGroupbuys = groupBuyService.getScrapGroupbuysByUser(user);
        return ResponseEntity.ok(scrapGroupbuys);
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

    //신고하기 - 작성
    @PostMapping(value = "/report", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportDto> createReport(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestBody ReportDto dto
    ){
        if(userDetail == null){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Long userId = userDetail.getId();
        return ResponseEntity.ok(mypageService.createReport(userId, dto));
    }

    // 내가 작성한 레시피 게시글 리스트
    @GetMapping("/my-boards")
    public ResponseEntity<List<BoardResponseDto>> getMyBoards(@AuthenticationPrincipal CustomUserDetail userDetail) {
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Long userId = userDetail.getId();
        List<BoardResponseDto> myBoards = boardService.getMyBoards(userId);
        return ResponseEntity.ok(myBoards);
    }

    // 내가 작성한 공동구매 게시글 리스트
    @GetMapping("/my-groupbuys")
    public ResponseEntity<List<GroupbuyResponseDto>> getMyGroupbuys(@AuthenticationPrincipal CustomUserDetail userDetail) {
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Long userId = userDetail.getId();
        List<GroupbuyResponseDto> myGroupbuys = groupBuyService.getMyGroupbuys(userId);
        return ResponseEntity.ok(myGroupbuys);
    }

    @GetMapping("/my-posts/count")
    public ResponseEntity<Map<String, Long>> getMyPostsCount(@AuthenticationPrincipal CustomUserDetail userDetail) {
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Long userId = userDetail.getId();

        // 각 서비스의 메서드를 호출하여 개수를 가져옴
        long recipePostCount = boardService.countMyBoards(userId);
        long groupBuyPostCount = groupBuyService.countMyGroupbuys(userId);

        Map<String, Long> response = new HashMap<>();
        response.put("recipePostCount", recipePostCount);
        response.put("groupBuyPostCount", groupBuyPostCount);
        response.put("totalPosts", recipePostCount + groupBuyPostCount);

        return ResponseEntity.ok(response);
    }

    // 내가 쓴 레시피 댓글 조회
    @GetMapping("/comments/recipe")
    public ResponseEntity<List<MypageDto>> getMyRecipeComments(@AuthenticationPrincipal CustomUserDetail userDetail) {
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // userDetail에서 사용자 ID를 직접 가져와 서비스에 전달
        List<MypageDto> myComments = recipeCommentService.getCommentsByUserId(userDetail.getId());
        return ResponseEntity.ok(myComments);
    }

    // 내가 쓴 공동구매 댓글 조회
    @GetMapping("/comments/groupbuy")
    public ResponseEntity<List<MyGroupBuyCommentDto>> getMyGroupBuyComments(@AuthenticationPrincipal CustomUserDetail userDetail) {
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // userDetail에서 사용자 ID를 직접 가져와 서비스에 전달
        List<MyGroupBuyCommentDto> myComments = groupBuyCommentService.getCommentsByUserId(userDetail.getId());
        return ResponseEntity.ok(myComments);
    }

    @GetMapping("/my-comments/count")
    public ResponseEntity<Map<String, Long>> getMyCommentCounts(@AuthenticationPrincipal CustomUserDetail userDetail) {
        if (userDetail == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Long userId = userDetail.getId();

        // 각 서비스의 메서드를 호출하여 댓글 개수를 가져옴
        long recipeCommentCount = recipeCommentService.getCommentCount(userId);
        long groupBuyCommentCount = groupBuyCommentService.getCommentCount(userId);

        Map<String, Long> response = new HashMap<>();
        response.put("recipeCommentCount", recipeCommentCount);
        response.put("groupBuyCommentCount", groupBuyCommentCount);
        response.put("totalCommentCount", recipeCommentCount + groupBuyCommentCount);

        return ResponseEntity.ok(response);
    }
}