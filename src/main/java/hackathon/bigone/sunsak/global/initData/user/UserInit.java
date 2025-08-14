package hackathon.bigone.sunsak.global.initData.user;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.accounts.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class UserInit {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Order(3) //초기 데이터 실행 순서
    public ApplicationRunner initUsers(){
        return args -> {
            //userRepository.deleteAll();
            if (userRepository.count() == 0) {
                insertDefaultUsers();
            }
        };
    }

    private void insertDefaultUsers() {
        userRepository.save(createUser(
                "짜파게티 요리사",
                "yorisa024",
                "dyfltk024@@"
        ));
        userRepository.save(createUser(
                "이주연",
                "juyeon123",
                "abc1234!!"
        ));
        userRepository.save(createUser(
                "집밥 마스터",
                "master777",
                "abc1234!!"
        ));

        System.out.println("초기 사용자 데이터 넣기 완료");
    }

    private SiteUser createUser(String nickname, String username, String rawPw) {
        return new SiteUser(nickname, username, passwordEncoder.encode(rawPw));
    }
}
