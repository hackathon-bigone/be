package hackathon.bigone.sunsak.accounts.user.service;

import hackathon.bigone.sunsak.accounts.user.dto.SignupRequestDto;
import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.accounts.user.repository.UserRepository;
import hackathon.bigone.sunsak.global.validate.accounts.SignupValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SignupValidator signupValidator;

    public SiteUser create(SignupRequestDto dto){
        signupValidator.validate(dto);

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        SiteUser user = SiteUser
                .builder()
                .nickname(dto.getNickname())
                .username(dto.getUsername())
                .password(encodedPassword)
                .build();
        return userRepository.save(user);
    }

    public Optional<SiteUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
