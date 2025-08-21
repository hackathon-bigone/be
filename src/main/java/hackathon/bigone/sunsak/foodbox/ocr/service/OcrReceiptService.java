package hackathon.bigone.sunsak.foodbox.ocr.service;

import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodBoxResponse;
import hackathon.bigone.sunsak.foodbox.foodbox.service.FoodBoxCommandService;
import hackathon.bigone.sunsak.foodbox.foodbox.service.FoodBoxQueryService;
import hackathon.bigone.sunsak.foodbox.nlp.service.NlpService;
import hackathon.bigone.sunsak.foodbox.ocr.dto.OcrExtractedItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
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

    //저장 전 보여주기 - upload
    public Map<String, Integer> showOnlyOutput(List<OcrExtractedItem> ocrItems) {
        Map<String, Integer> finalCount = new LinkedHashMap<>();
        if (ocrItems == null || ocrItems.isEmpty()) return finalCount;

        for (OcrExtractedItem line : ocrItems) {
            log.debug("[PIPE] raw='{}' qty={}", line.getName(), line.getQuantity());

            // 핵심명 추출
            String core = extractCoreByStar(line.getName());
            log.debug("[PIPE]  -> core='{}'", core);
            if (core == null || core.isBlank()) continue;

            // 하나의 라인에 quantity 그대로
            OcrExtractedItem pivot = new OcrExtractedItem(core, line.getQuantity());
            NlpService.ClassifiedTokens classified = nlpService.classifyByUserDict(List.of(pivot));

            log.debug("[PIPE]  NLP userDictKeys={}, freeNounKeys={}",
                    (classified.getUserDict() != null ? classified.getUserDict().keySet() : List.of()),
                    (classified.getFreeNouns() != null ? classified.getFreeNouns().keySet() : List.of()));

            // 최종 표준명 선택
            String stdName = pickOne(classified, core);

            if (stdName == null || stdName.isBlank()) {
                log.debug("[PIPE]  ~~ SKIP (unmapped) core='{}'", core); // ★ 매핑 실패 시 건너뜀
                continue;
            }

            log.debug("[PIPE]  ==> chosen='{}' +{}", stdName, line.getQuantity());
            finalCount.merge(stdName, line.getQuantity(), Integer::sum);
        }

        log.debug("[PIPE] finalCount={}", finalCount);
        return finalCount;
    }

    //저장할 때
    @Transactional
    public List<FoodBoxResponse> saveFromOcr(Long userId, List<OcrExtractedItem> ocrItems) {
        if (userId == null) throw new IllegalArgumentException("userId가 없습니다.");
        if (ocrItems == null || ocrItems.isEmpty()) return queryService.getFoodsByUserList(userId);

        Map<String, Integer> finalCount = showOnlyOutput(ocrItems);
        if (finalCount.isEmpty()) return queryService.getFoodsByUserList(userId);

        // 유통기한 조회
        Map<String, Integer> daysByName = new HashMap<>();
        for (String name : finalCount.keySet()) {
            daysByName.put(name, lookupExpiryDays(name));
        }

        // 업서트
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

    private String extractCoreByStar(String raw) {
        if (raw == null) return "";
        String s = raw.trim().replaceAll("\\s+", " ");

        //영수증 '*' 표 사이의 문자
        if (s.contains("*")) {
            String[] parts = s.split("\\*+");
            for (String part : parts) {
                if (part == null) continue;
                String p = part.trim();
                // 앞 번호/공백 제거
                p = p.replaceFirst("^\\d+\\s*", "");
                // 한글 포함하는 첫 세그먼트만 채택
                if (p.matches(".*[가-힣].*")) {
                    // 앞/뒤의 비-한글 제거, 내부 공백 제거
                    p = p.replaceFirst("^[^가-힣]+", "")
                            .replaceFirst("[^가-힣]+$", "")
                            .replaceAll("\\s+", "");
                    if (!p.isBlank()) return p;
                }
            }
        }

        String korean = raw.replaceAll("[^가-힣]", " ")
                .replaceAll("\\s+", "");
        return korean;
    }

    private String pickOne(NlpService.ClassifiedTokens classified, String core) {
        // user_dict 우선
        if (classified != null && classified.getUserDict() != null && !classified.getUserDict().isEmpty()) {
            return classified.getUserDict().keySet().stream()
                    .filter(Objects::nonNull).map(String::trim).filter(s -> !s.isBlank())
                    .max(Comparator.comparingInt(String::length))
                    .orElse(core);
        }

        // 자유명사 → 표준명 매핑
        if (classified != null && classified.getFreeNouns() != null && !classified.getFreeNouns().isEmpty()) {
            Map<String, String> mapped = normalizationService.normalizeFreeNouns(
                    new ArrayList<>(classified.getFreeNouns().keySet()));
            if (mapped != null && !mapped.isEmpty()) {
                return mapped.values().stream()
                        .filter(Objects::nonNull).map(String::trim).filter(s -> !s.isBlank())
                        .findFirst().orElse(core);
            }
        }

        // core에 alias
        Map<String, String> alias = normalizationService.normalizeFreeNouns(List.of(core));
        String a = (alias != null ? alias.get(core) : null);

        return (a != null && !a.isBlank()) ? a : null;
    }

    // 품종 우선 → 없으면 대표식품
    private int lookupExpiryDays(String name) {
        String v = redisTemplate.opsForValue().get(EXPIRY_PREFIX + "variety:" + name);
        if (isNumeric(v)) return Integer.parseInt(v);

        v = redisTemplate.opsForValue().get(EXPIRY_PREFIX + "item:" + name);
        if (isNumeric(v)) return Integer.parseInt(v);

        return DEFAULT_EXPIRY_DAYS;
    }

    private boolean isNumeric(String s) {
        if (s == null || s.isBlank()) return false;
        for (int i = 0; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return false;
        return true;
    }
}
