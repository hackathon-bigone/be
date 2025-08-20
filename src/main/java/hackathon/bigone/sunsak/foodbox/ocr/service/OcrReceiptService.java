package hackathon.bigone.sunsak.foodbox.ocr.service;

import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodBoxResponse;
import hackathon.bigone.sunsak.foodbox.foodbox.service.FoodBoxCommandService;
import hackathon.bigone.sunsak.foodbox.foodbox.service.FoodBoxQueryService;
import hackathon.bigone.sunsak.foodbox.nlp.service.NlpService;
import hackathon.bigone.sunsak.foodbox.ocr.dto.OcrExtractedItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OcrReceiptService {
    private final StringRedisTemplate redisTemplate;
    private final NlpService nlpService;                       // Komoran
    private final OcrNomalizationService normalizationService; // 자유명사 → 표준명 매핑
    private final FoodBoxCommandService commandService;        // 실제 저장
    private final FoodBoxQueryService queryService;            // 저장 후 목록 반환

    private static final String EXPIRY_PREFIX = "expiry:";
    private static final int DEFAULT_EXPIRY_DAYS = 0; // 0/없음 → null

    @Transactional
    public List<FoodBoxResponse> saveFromOcr(Long userId, List<OcrExtractedItem> ocrItems) {
        if (userId == null) throw new IllegalArgumentException("userId가 없습니다.");
        if (ocrItems == null || ocrItems.isEmpty()) return queryService.getFoodsByUserList(userId);

        Map<String, Integer> finalCount = new LinkedHashMap<>();

        for (OcrExtractedItem line : ocrItems) {
            // 라인 단위로 분류
            NlpService.ClassifiedTokens classifiedLine = nlpService.classifyByUserDict(List.of(line));

            // 라인 내에서 표준명 중복 제거
            Set<String> stdNamesInLine = new LinkedHashSet<>();

            // user_dict에 잡힌 표준명
            if (classifiedLine != null && classifiedLine.getUserDict() != null) {
                Map<String, Integer> ud = classifiedLine.getUserDict(); // 표준명 → 빈도/점수 등
                for (String std : ud.keySet()) {                        // ⭐️ values() → keySet()
                    String s = normalizeKey(std);
                    if (!s.isBlank()) stdNamesInLine.add(s);
                }
            }

            // 자유명사 → 표준명 매핑
            if (classifiedLine != null && classifiedLine.getFreeNouns() != null && !classifiedLine.getFreeNouns().isEmpty()) {
                Map<String, String> mappedFree = normalizationService.normalizeFreeNouns(
                        new ArrayList<>(classifiedLine.getFreeNouns().keySet())
                );
                if (mappedFree != null) {
                    for (String std : mappedFree.values()) {
                        if (std != null && !std.isBlank()) stdNamesInLine.add(std);
                    }
                }
            }

            int lineQty = line.getQuantity();

            for (String std : stdNamesInLine) {
                finalCount.merge(std, lineQty, Integer::sum);
            }
        }

        if (finalCount.isEmpty()) return queryService.getFoodsByUserList(userId);

        // 이름별 유통기한 일수 조회 (품종 우선 → 대표식품 폴백)
        Map<String, Integer> daysByName = new HashMap<>();
        for (String name : finalCount.keySet()) {
            daysByName.put(name, lookupExpiryDays(name));
        }

        // (이름, 유통기한날짜) 기준으로 수량 합쳐서 업서트
        LocalDate today = LocalDate.now();
        List<FoodBoxCommandService.FoodUpsertItem> aggregated = new ArrayList<>();
        for (var e : finalCount.entrySet()) {
            String name = e.getKey();
            int qty = e.getValue(); // 라인 수 기준
            int days = daysByName.getOrDefault(name, DEFAULT_EXPIRY_DAYS);
            LocalDate expiry = (days <= 0) ? null : today.plusDays(days);
            aggregated.add(new FoodBoxCommandService.FoodUpsertItem(name, qty, expiry));
        }
        commandService.upsertAggregated(userId, aggregated);

        // 최신 목록 반환
        return queryService.getFoodsByUserList(userId);
    }

    private String normalizeKey(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t;
    }

    // 품종 우선 → 없으면 대표식품
    private int lookupExpiryDays(String name) {
        String v = redisTemplate.opsForValue().get("expiry:variety:" + name);
        if (isNumeric(v)) return Integer.parseInt(v);
        v = redisTemplate.opsForValue().get("expiry:item:" + name);
        if (isNumeric(v)) return Integer.parseInt(v);
        return DEFAULT_EXPIRY_DAYS;
    }

    private boolean isNumeric(String s) {
        if (s == null || s.isBlank()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }
}
