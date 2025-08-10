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

        // Komoran 분석 + user_dict / 자유명사 분류
        NlpService.ClassifiedTokens classified = nlpService.classifyByUserDict(ocrItems);
        Map<String, Integer> userDictGroup = classified.getUserDict();   // 이미 표준명
        Map<String, Integer> freeNounGroup = classified.getFreeNouns();  // 자유명사

        // 자유명사 → 표준명 매핑
        Map<String, String> mappedFree = normalizationService.normalizeFreeNouns(
                new ArrayList<>(freeNounGroup.keySet())
        );

        // 표준명 기준 최종 수량 집계
        Map<String, Integer> finalCount = new LinkedHashMap<>();
        userDictGroup.forEach((k, v) -> finalCount.merge(k, v, Integer::sum));
        for (var e : freeNounGroup.entrySet()) {
            String std = mappedFree.get(e.getKey());
            if (std == null) continue; // 매핑 실패 항목은 버림
            finalCount.merge(std, e.getValue(), Integer::sum);
        }
        if (finalCount.isEmpty()) return queryService.getFoodsByUserList(userId);

        // 이름별 유통기한 일수 Redis 조회
        List<String> names = new ArrayList<>(finalCount.keySet());
        List<String> expiryKeys = names.stream().map(n -> EXPIRY_PREFIX + n).toList();
        List<String> expiryVals = redisTemplate.opsForValue().multiGet(expiryKeys);

        Map<String, Integer> daysByName = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {
            String raw = (expiryVals != null && i < expiryVals.size()) ? expiryVals.get(i) : null;
            daysByName.put(names.get(i), parseExpiryDays(raw));
        }

        // (이름, 유통기한날짜) 기준으로 수량 합쳐서 업서트
        LocalDate today = LocalDate.now();
        List<FoodBoxCommandService.FoodUpsertItem> aggregated = new ArrayList<>();
        for (var e : finalCount.entrySet()) {
            String name = e.getKey();
            int qty = e.getValue();
            int days = daysByName.getOrDefault(name, DEFAULT_EXPIRY_DAYS);
            LocalDate expiry = (days <= 0) ? null : today.plusDays(days);
            aggregated.add(new FoodBoxCommandService.FoodUpsertItem(name, qty, expiry));
        }
        commandService.upsertAggregated(userId, aggregated);

        // 최신 목록 반환
        return queryService.getFoodsByUserList(userId);
    }

    private int parseExpiryDays(String s) {
        if (s == null || s.isBlank()) return DEFAULT_EXPIRY_DAYS;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return DEFAULT_EXPIRY_DAYS; }
    }
}

