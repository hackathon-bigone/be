package hackathon.bigone.sunsak.global.redis.controller;

import hackathon.bigone.sunsak.global.redis.RedisFoodSaver;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/admin/redis")
@RequiredArgsConstructor
public class RedisResetController {
    private final RedisFoodSaver redisFoodSaver;

    @PostMapping("/reset")
    public ResponseEntity<String> redisReset(){
        Path temp = null;
        try (InputStream is = new ClassPathResource("data/food_data.csv").getInputStream()) {
            // JAR 내부 리소스를 임시파일로 복사
            temp = Files.createTempFile("food_data_", ".csv");
            Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);

            // 기존 시그니처 그대로 사용
            redisFoodSaver.saveWithReset(temp.toString());

            return ResponseEntity.ok("Redis 초기화 완료");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Redis 초기화 실패: " + e.getMessage());
        } finally {
            if (temp != null) {
                try { Files.deleteIfExists(temp); } catch (IOException ignored) {}
            }
        }
    }
}