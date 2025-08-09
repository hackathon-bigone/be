package hackathon.bigone.sunsak.foodbox.foodbox.service;

import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodBoxResponse;
import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodItemRequest;
import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodListResponse;
import hackathon.bigone.sunsak.foodbox.foodbox.dto.update.FoodItemUpdateRequest;
import hackathon.bigone.sunsak.foodbox.foodbox.entity.FoodBox;
import hackathon.bigone.sunsak.foodbox.foodbox.repository.FoodBoxRepository;
import hackathon.bigone.sunsak.foodbox.nlp.service.NlpService;
import hackathon.bigone.sunsak.foodbox.ocr.dto.OcrExtractedItem;
import hackathon.bigone.sunsak.foodbox.ocr.service.OcrNomalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodBoxService {

    private final StringRedisTemplate redisTemplate;
    private final FoodBoxRepository foodBoxRepository;
    private final NlpService nlpService;                       // Komoran 분석
    private final OcrNomalizationService normalizationService; // 자유명사 Redis keyword 매핑

    private static final String EXPIRY_PREFIX = "expiry:";
    private static final int DEFAULT_EXPIRY_DAYS = 0; // 0/없음 → null(빈칸)

    private static final ZoneId KST = ZoneId.of("Asia/Seoul"); //한국날짜
    private static final DateTimeFormatter KOREAN_DATE = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"); //헤더
    private static final int DEFAULT_IMMINENT_DAYS = 7;

    /**
     * OCR 기반 저장
     * - user_dict 그룹: 그대로 저장
     * - 자유명사 그룹: Redis keyword 매핑 성공 시 저장
     * - 두 그룹 모두 Redis expiry 매핑
     */
    @Transactional
    public List<FoodBoxResponse> saveFromOcr(Long userId, List<OcrExtractedItem> ocrItems) {
        if (userId == null) throw new IllegalArgumentException("userId가 없습니다.");
        if (ocrItems == null || ocrItems.isEmpty()) return getFoodsByUserList(userId);

        // Komoran 분석 + user_dict / freeNoun 그룹 분류
        NlpService.ClassifiedTokens classified = nlpService.classifyByUserDict(ocrItems);
        Map<String, Integer> userDictGroup = classified.getUserDict();
        Map<String, Integer> freeNounGroup = classified.getFreeNouns();

        // 자유명사 → Redis keyword 매핑
        Map<String, String> mappedFree = normalizationService.normalizeFreeNouns(
                new ArrayList<>(freeNounGroup.keySet())
        );

        // 최종 표준명 → 수량 합산
        Map<String, Integer> finalCount = new LinkedHashMap<>();
        // user_dict 그룹
        for (var e : userDictGroup.entrySet()) {
            finalCount.merge(e.getKey(), e.getValue(), Integer::sum);
        }
        // 매핑된 자유명사
        for (var e : freeNounGroup.entrySet()) {
            String std = mappedFree.get(e.getKey());
            if (std == null) continue; // 매핑 실패 시 버림
            finalCount.merge(std, e.getValue(), Integer::sum);
        }

        if (finalCount.isEmpty()) return getFoodsByUserList(userId);

        // Redis expiry 조회
        List<String> names = new ArrayList<>(finalCount.keySet());
        List<String> expiryKeys = names.stream().map(n -> EXPIRY_PREFIX + n).toList();
        List<String> expiryVals = redisTemplate.opsForValue().multiGet(expiryKeys);

        Map<String, Integer> daysByName = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {
            String raw = (expiryVals != null && i < expiryVals.size()) ? expiryVals.get(i) : null;
            daysByName.put(names.get(i), parseExpiryDays(raw));
        }

        // 저장 (OCR은 name+expiry 기준 병합)
        LocalDate today = LocalDate.now();
        record Key(String name, LocalDate expiry) {}
        Map<Key, Integer> merged = new LinkedHashMap<>();

        for (var e : finalCount.entrySet()) {
            String name = e.getKey();
            int qty = e.getValue();
            int days = daysByName.getOrDefault(name, DEFAULT_EXPIRY_DAYS);
            LocalDate expiry = (days <= 0) ? null : today.plusDays(days);

            merged.merge(new Key(name, expiry), qty, Integer::sum);
        }

        for (var e : merged.entrySet()) {
            var k = e.getKey();
            int qty = e.getValue();

            var existing = foodBoxRepository.findByUserIdAndNameAndExpiryDate(userId, k.name(), k.expiry());
            if (existing.isPresent()) {
                existing.get().setQuantity(existing.get().getQuantity() + qty);
            } else {
                foodBoxRepository.save(FoodBox.builder()
                        .userId(userId)
                        .name(k.name())
                        .quantity(qty)
                        .expiryDate(k.expiry())
                        .build());
            }
        }

        return getFoodsByUserList(userId);
    }

    private int parseExpiryDays(String s) {
        if (s == null || s.isBlank()) return DEFAULT_EXPIRY_DAYS;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return DEFAULT_EXPIRY_DAYS; }
    }

    // 기존 수동 저장 로직
    @Transactional
    public List<FoodBoxResponse> saveFoods(Long userId, List<FoodItemRequest> items) {
        if (userId == null) throw new IllegalArgumentException("userId가 없습니다.");
        if (items == null || items.isEmpty()) return getFoodsByUserList(userId);

        for (FoodItemRequest it : items) {
            if (it == null || it.getName() == null || it.getName().isBlank()) continue;
            if (it.getExpiryDate() == null)
                throw new IllegalArgumentException("유통기한 기간을 입력해주세요.");

            foodBoxRepository.save(FoodBox.builder()
                    .userId(userId)
                    .name(it.getName().trim())
                    .quantity(Math.max(1, it.getQuantity()))
                    .expiryDate(it.getExpiryDate())
                    .build());
        }

        return getFoodsByUserList(userId);
    }

    // 로그인한 유저의 foodbox
    @Transactional(readOnly = true)
    public FoodListResponse getFoodsByUser(Long userId, String filter, Integer days) {
        if (userId == null) throw new IllegalArgumentException("userId가 없습니다.");
        int threshold = (days == null) ? DEFAULT_IMMINENT_DAYS : days;
        if (threshold < 0) throw new IllegalArgumentException("days 는 0 이상이어야 합니다.");

        LocalDate today = LocalDate.now(KST);
        String todayStr = today.format(KOREAN_DATE);

        List<FoodBox> rows = foodBoxRepository.findAllSortedByUserId(userId);

        boolean isImminent = "imminent".equalsIgnoreCase(filter);
        List<FoodBoxResponse> items;

        if (isImminent) {
            // 유통기한 있는 것만, daysLeft <= threshold
            items = rows.stream()
                    .filter(f -> f.getExpiryDate() != null)
                    .map(f -> {
                        long d = ChronoUnit.DAYS.between(today, f.getExpiryDate()); // 오늘=0, 지나면 음수
                        return FoodBoxResponse.builder()
                                .foodId(f.getId())
                                .name(f.getName())
                                .quantity(f.getQuantity())
                                .expiryDate(f.getExpiryDate())
                                .daysLeft((int) d)
                                .imminent(d <= threshold)
                                .build();
                    })
                    .filter(r -> r.getDaysLeft() != null && r.getDaysLeft() <= threshold)
                    .sorted(Comparator
                            .comparing(FoodBoxResponse::getDaysLeft)
                            .thenComparing(FoodBoxResponse::getName, Comparator.nullsLast(Comparator.naturalOrder()))
                    )
                    .toList();
        } else {
            // 전체조회 - 디데이 없음
            items = rows.stream()
                    .map(f -> FoodBoxResponse.builder()
                            .foodId(f.getId())
                            .name(f.getName())
                            .quantity(f.getQuantity())
                            .expiryDate(f.getExpiryDate())
                            .daysLeft(null)     // 전체에선 숨김
                            .imminent(false)
                            .build())
                    .sorted(Comparator
                            .comparing(FoodBoxResponse::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(FoodBoxResponse::getName, Comparator.nullsLast(Comparator.naturalOrder()))
                    )
                    .toList();
        }

        return new FoodListResponse(todayStr, items);
    }

    @Transactional(readOnly = true) //내부 조회용
    public List<FoodBoxResponse> getFoodsByUserList(Long userId) {
        return getFoodsByUser(userId, "all", DEFAULT_IMMINENT_DAYS).getItems();
    }

    //수정하기 boolean dryRun
    @Transactional
    public void batchUpdate(Long userId, List<FoodItemUpdateRequest> items) {
        if (items == null || items.isEmpty()) return;

        // foodbox list 가져오기
        List<Long> ids = items.stream().map(FoodItemUpdateRequest::getFoodId).toList();
        List<FoodBox> entities = foodBoxRepository.findAllById(ids);
        Map<Long, FoodBox> map = entities.stream().collect(Collectors.toMap(FoodBox::getId, e -> e));

        for (FoodItemUpdateRequest it : items) {
            FoodBox e = map.get(it.getFoodId());
            if (e == null) {
                throw new IllegalArgumentException("food_id not found: " + it.getFoodId());
            }
            if (!e.getUserId().equals(userId)) {
                throw new AccessDeniedException("not your item: " + it.getFoodId());
            }

            // 이름 필수
            if (it.getName() == null || it.getName().isBlank()) {
                throw new IllegalArgumentException("이름은 필수입니다.");
            }
            e.setName(it.getName());

            // 수량: int → 기본값 0 저장
            e.setQuantity(it.getQuantity());

            // 유통기한: null이면 유지
            if (it.getExpiryDate() != null) {
                e.setExpiryDate(it.getExpiryDate());
            }
        }

        // 3) 저장
        foodBoxRepository.saveAll(entities);
    }


    //삭제
    public boolean delete(Long userId, List<Long> foodIds) {
        if(foodIds == null || foodIds.isEmpty()) return false;

        List<FoodBox> deleteFood = foodBoxRepository.findAllByIdIn(foodIds)
                .stream()
                .filter(f-> f.getUserId().equals(userId))
                .toList();

        if (deleteFood.isEmpty()) {
            return false;
        }

        foodBoxRepository.deleteAll(deleteFood);
        return true;
    }
}

