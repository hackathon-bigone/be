package hackathon.bigone.sunsak.accounts.mypage.controller;

import hackathon.bigone.sunsak.accounts.mypage.dto.NoticeDto;
import hackathon.bigone.sunsak.accounts.mypage.dto.PasswordChangeDto;
import hackathon.bigone.sunsak.accounts.mypage.service.MypageService;
import hackathon.bigone.sunsak.global.security.jwt.CustomUserDetail;
import hackathon.bigone.sunsak.global.validate.accounts.SignupValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {
    private final SignupValidator signupValidator;
    private final MypageService mypageService;

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
}
