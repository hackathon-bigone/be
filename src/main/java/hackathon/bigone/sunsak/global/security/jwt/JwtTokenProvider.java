package hackathon.bigone.sunsak.global.security.jwt;

import hackathon.bigone.sunsak.global.security.jwt.dto.JwtTokenDto;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    private final long ACCESS_TOKEN_VALID_TIME = 1000 * 60 * 60 * 3; // 3시간
    private final long REFRESH_TOKEN_VALID_TIME = 1000L * 60 * 60 * 24 * 7; // 7일

    private final UserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey); // 👈 Base64 디코딩 추가
        this.key = Keys.hmacShaKeyFor(decodedKey);
    }

    public JwtTokenDto createToken(String username) {
        Date now = new Date();
        Date accessExpires = new Date(now.getTime() + ACCESS_TOKEN_VALID_TIME); //만료일
        Date refreshExpires = new Date(now.getTime() + REFRESH_TOKEN_VALID_TIME);

        String accessToken = Jwts.builder() //accessToken 생성
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(accessExpires)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder() //refreshToken 생성
                .setExpiration(refreshExpires)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtTokenDto.builder() //응답 보여주기
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Nullable
    public Authentication getAuthentication(String token) {
        try {
            String username = getUserPk(token);
            if (username == null || username.isBlank()) return null;
            var userDetails = userDetailsService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
        } catch (UsernameNotFoundException ex) {
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    public String getUserPk(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getRemainingExpiration(String token) {
        try {
            var claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
            Date exp = claims.getExpiration();
            long ttl = (exp == null) ? 0L : (exp.getTime() - System.currentTimeMillis());
            return Math.max(ttl, 0L);
        } catch (Exception e) {
            return 0L;
        }
    }

    public String resolveToken(HttpServletRequest req) {
        String b = req.getHeader("Authorization");
        if (b == null || !b.startsWith("Bearer ")) return null;
        String t = b.substring(7).trim();
        if (t.isEmpty()) return null;
        if ("null".equalsIgnoreCase(t) || "undefined".equalsIgnoreCase(t)) return null; // ✨
        return t;
    }

    public long getRemainingTtlMillis(String token) {
        try {
            var claimsJws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            var exp = claimsJws.getBody().getExpiration();
            long ttl = exp.getTime() - System.currentTimeMillis();
            return Math.max(ttl, 0L);
        } catch (Exception e) {
            return 0L; // 만료/깨짐 → 0
        }
    }

}

