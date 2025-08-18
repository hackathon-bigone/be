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

                // keyword 저장
//                if (!variety.isEmpty()) {
//                    String varietyKey = "keyword:" + variety;
//                    if (!redisTemplate.hasKey(varietyKey)) {
//                        redisTemplate.opsForValue().set(varietyKey, "대표식품명:" + mainName);
//                    }
//                }
                if (!variety.isEmpty()) {
                    String varietyKey = "keyword:" + variety;
                    redisTemplate.opsForValue().set(varietyKey, "품종명:" + variety);
                }

//                if (!mainName.isEmpty()) {
//                    String mainKey = "keyword:" + mainName;
//                    if (!redisTemplate.hasKey(mainKey)) {
//                        redisTemplate.opsForValue().set(mainKey, "품종명:" + variety);
//                    }
//                }
                if (!mainName.isEmpty()) {
                    String mainKey = "keyword:" + mainName;
                    redisTemplate.opsForValue().set(mainKey, "품목명:" + mainName);
                }

                // 유통기한 저장 (숫자인 경우만)
                if (expiry.matches("\\d+")) {
                    if (!mainName.isEmpty()) {
                        redisTemplate.opsForValue().set("expiry:" + mainName, expiry);
                    }
                    if (!variety.isEmpty()) {
                        redisTemplate.opsForValue().set("expiry:" + variety, expiry);
                    }
                }
            }

            System.out.println("데이터 저장 완료");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearKeywordData() {
        Set<String> keys1 = redisTemplate.keys("keyword:*");
        Set<String> keys2 = redisTemplate.keys("expiry:*");

        if (keys1 != null && !keys1.isEmpty()) {
            redisTemplate.delete(keys1);
            System.out.println("기존 keyword:* 데이터 삭제 완료!");
        }
        if (keys2 != null && !keys2.isEmpty()) {
            redisTemplate.delete(keys2);
            System.out.println("기존 expiry:* 데이터 삭제 완료!");
        }
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
