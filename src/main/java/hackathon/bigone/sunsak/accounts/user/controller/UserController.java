package hackathon.bigone.sunsak.accounts.user.controller;

import hackathon.bigone.sunsak.accounts.user.dto.LoginRequestDto;
import hackathon.bigone.sunsak.accounts.user.dto.SignupRequestDto;
import hackathon.bigone.sunsak.accounts.user.dto.SignupResponseDto;
import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.accounts.user.repository.UserRepository;
import hackathon.bigone.sunsak.accounts.user.service.LogoutService;
import hackathon.bigone.sunsak.accounts.user.service.SignupService;
import hackathon.bigone.sunsak.global.security.jwt.JwtTokenProvider;
import hackathon.bigone.sunsak.global.security.jwt.dto.JwtTokenDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/user")
@RestController
@AllArgsConstructor
public class UserController {
    private final SignupService signupService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final LogoutService logoutService;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody SignupRequestDto rqDto){
        SiteUser user = signupService.create(rqDto);

        SignupResponseDto rsDto = SignupResponseDto
                .builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .message("회원가입이 완료되었습니다.")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(rsDto);
    }

    @GetMapping("/check-username")
    public ResponseEntity<String> checkUsername(@RequestParam String username) {
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.ok("중복된 아이디입니다.");
        }
        return ResponseEntity.ok("사용 가능한 아이디입니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
        SiteUser user = signupService.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("아이디와 비밀번호를 다시 확인하세요."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("아이디와 비밀번호를 다시 확인하세요.");
        }

        JwtTokenDto token = jwtTokenProvider.createToken(dto.getUsername());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "유효하지 않은 토큰입니다."));
        }

        long remainingTime = jwtTokenProvider.getRemainingExpiration(token);
        logoutService.blacklistToken(token, remainingTime);

        return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
    }
}
