package hackathon.bigone.sunsak.global.security;

import hackathon.bigone.sunsak.accounts.user.service.LogoutService;
import hackathon.bigone.sunsak.global.security.jwt.JwtAuthenticationFilter;
import hackathon.bigone.sunsak.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final LogoutService logoutService;
    //private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean //나중에 url별로 요청 권한 바꾸기
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(
                        org.springframework.security.config.http.SessionCreationPolicy.STATELESS)) // 세션 사용 안함
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) //CORS
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/user/login", "/user/signup").permitAll() // 1) 로그인, 회원가입은 제외
                                // .requestMatchers(HttpMethod.POST, "/**").authenticated() // POST는 인증 필요

                                // 2) 영수증 업로드, 식품 저장: POST만 인증 필요 , 목록보기 후에 추가
                                .requestMatchers(HttpMethod.POST, "/foodbox/receipt/upload").authenticated()
                                .requestMatchers(HttpMethod.POST, "/foodbox/save").authenticated()

                                .anyRequest().permitAll() // 나머지는 일단 허용
                                // mypage, 식품 보관함 목록 보기 -> 인증 필요
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, logoutService),
                        UsernamePasswordAuthenticationFilter.class)
                //.exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*"); // 모든 Origin 허용 (배포 시 도메인 제한 가능)
        config.addAllowedMethod("*"); // GET, POST, PUT, DELETE, OPTIONS
        config.addAllowedHeader("*"); // 모든 헤더 허용
        config.setAllowCredentials(true); // 인증정보 허용 (JWT 같은거 보낼 때 필요)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }


}
