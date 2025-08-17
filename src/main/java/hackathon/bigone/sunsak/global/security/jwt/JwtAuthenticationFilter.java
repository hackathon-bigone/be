package hackathon.bigone.sunsak.global.security.jwt;

import hackathon.bigone.sunsak.accounts.user.service.LogoutService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final LogoutService logoutService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = jwtTokenProvider.resolveToken(request);

            if (token != null) {
                // 블랙리스트면 인증 세팅만 하지 말고 그대로 통과 (응답 X, return X)
                if (logoutService.isBlacklisted(token)) {
                    SecurityContextHolder.clearContext();
                } else if (jwtTokenProvider.validateToken(token)) {
                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                    if (auth != null) {
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        SecurityContextHolder.clearContext();
                    }
                } else {
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext(); // 예외 터져도 500 내지 말고
        }
        chain.doFilter(request, response);
    }
}

