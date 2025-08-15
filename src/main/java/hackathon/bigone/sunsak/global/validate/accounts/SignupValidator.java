package hackathon.bigone.sunsak.global.validate.accounts;

import hackathon.bigone.sunsak.accounts.user.dto.SignupRequestDto;
import hackathon.bigone.sunsak.accounts.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignupValidator {

    private final UserRepository userRepository;

    public  void validate(SignupRequestDto dto){
        nicknameValidate(dto.getNickname());
        usernameValidate(dto.getUsername());
        passwordValidate(dto.getPassword(), dto.getRepeatPw());
    }

    public void nicknameValidate(String nickname){
        String regex = "^[가-힣a-zA-Z\\s]{2,10}$";
        if (!nickname.matches(regex)) {
            throw new IllegalArgumentException("공백 포함 2~10자의 영문 또는 한글을 입력하세요.");
        }
    }

    private void usernameValidate(String username){
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{5,12}$";
        if(!username.matches(regex)){
            throw new IllegalArgumentException("영문, 숫자 포함 5~12자를 입력하세요.");
        }
    }

    public void passwordValidate(String password, String repeatPw){
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=`~{}\\[\\]:\";'<>?,./]).{8,16}$";
        if (!password.matches(regex)) {
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자를 포함한 8~16자를 입력하세요.");
        }

        if (!password.equals(repeatPw)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }
}
