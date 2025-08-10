package hackathon.bigone.sunsak.accounts.mypage;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.accounts.user.repository.UserRepository;
import hackathon.bigone.sunsak.global.validate.accounts.SignupValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MypageService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SignupValidator signupValidator;

    @Transactional
    public void updateNickname(Long userId, String newNickname) {
        SiteUser user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getNickname().equals(newNickname)) {
            throw new IllegalArgumentException("현재 사용 중인 닉네임과 동일합니다.");
        }

        user.setNickname(newNickname);
    }

    @Transactional
    public void updatePassword(Long userId, PasswordChangeDto dto){
        SiteUser user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if(!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())){
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
        }

        signupValidator.passwordValidate(dto.getNewPassword(), dto.getRepeatPw());

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }


}
