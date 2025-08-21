package hackathon.bigone.sunsak.global.redis;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Set;

@Component
@AllArgsConstructor
public class RedisFoodSaver {
    private final StringRedisTemplate redisTemplate;

    public void saveToRedis(String filePath) {
        try (
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(filePath), Charset.forName("UTF-8")
                        )
                )
        ) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (first || line.contains("대표식품명")) {
                    first = false;
                    System.out.println("헤더 건너뜀: " + line);
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 2) {
                    System.out.println("잘못된 줄: " + line);
                    continue;
                }

                String mainName = parts[0].trim();    //대표 식품명
                String variety = parts[1].trim();     //품종명
                String expiry = (parts.length >= 3) ? parts[2].trim() : ""; //유통기한 없으면 빈문자열

                //대표 식품명만 redis
                if (!mainName.isEmpty()) {
                    String mainKey = "keyword:" + mainName;
                    redisTemplate.opsForValue().set(mainKey, "대표식품명:" + mainName); //대표식품명
                }

                // 유통기한 저장
                if (expiry.matches("\\d+")) {
                    if (!mainName.isEmpty()) {
                        //대표 식품명
                        redisTemplate.opsForValue().set("expiry:item:" + mainName, expiry);
                    }
                    if (!variety.isEmpty()) {
                        //품종명
                        redisTemplate.opsForValue().set("expiry:variety:" + variety, expiry);
                    }
                }
            }
            System.out.println("데이터 저장 완료");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearKeywordData() {
        var k1 = redisTemplate.keys("keyword:*");
        var k2 = redisTemplate.keys("expiry:item:*");
        var k3 = redisTemplate.keys("expiry:variety:*");

        if (k1 != null && !k1.isEmpty()) redisTemplate.delete(k1);
        if (k2 != null && !k2.isEmpty()) redisTemplate.delete(k2);
        if (k3 != null && !k3.isEmpty()) redisTemplate.delete(k3);

        System.out.println("기존 keyword/expiry 데이터 삭제 완료");
    }

    public void saveWithReset(String filePath) {
        clearKeywordData();
        saveToRedis(filePath);
    }

    public boolean isSaved() {
        Set<String> keys = redisTemplate.keys("keyword:*");
        return keys != null && !keys.isEmpty();
    }
}
