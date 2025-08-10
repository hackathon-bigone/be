package hackathon.bigone.sunsak.foodbox.foodbox.service;

import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodBoxResponse;
import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodItemRequest;
import hackathon.bigone.sunsak.foodbox.foodbox.dto.update.FoodItemUpdateRequest;
import hackathon.bigone.sunsak.foodbox.foodbox.entity.FoodBox;
import hackathon.bigone.sunsak.foodbox.foodbox.repository.FoodBoxRepository;
import hackathon.bigone.sunsak.foodbox.nlp.service.NlpService;
import hackathon.bigone.sunsak.foodbox.ocr.dto.OcrExtractedItem;
import hackathon.bigone.sunsak.foodbox.ocr.service.OcrNomalizationService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodBoxCommandService {

    private final StringRedisTemplate redisTemplate;
    private final FoodBoxRepository foodBoxRepository;
    private final NlpService nlpService;                       // Komoran 분석
    private final OcrNomalizationService normalizationService; // 자유명사 Redis keyword 매핑

    private static final String EXPIRY_PREFIX = "expiry:";
    private static final int DEFAULT_EXPIRY_DAYS = 0; // 0/없음 → null(빈칸)

    private final FoodBoxQueryService foodBoxQueryService; // 조회

    @Getter
    @AllArgsConstructor
    public static class FoodUpsertItem {
        private String name;
        private int quantity;
        private LocalDate expiryDate; // null 가능
    }


    // 사용자가 식품 직접 추가
    @Transactional
    public List<FoodBoxResponse> saveFoods(Long userId, List<FoodItemRequest> items) {
        if (userId == null) throw new IllegalArgumentException("userId가 없습니다.");
        if (items == null || items.isEmpty()) return foodBoxQueryService.getFoodsByUserList(userId);

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

        return foodBoxQueryService.getFoodsByUserList(userId);
    }

    //식품 수정하기
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

        // 저장
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

    @Transactional
    public void upsertAggregated(Long userId, List<FoodUpsertItem> aggregated) {
        for (FoodUpsertItem item : aggregated) {
            var existing = foodBoxRepository.findByUserIdAndNameAndExpiryDate(userId, item.getName(), item.getExpiryDate());
            if (existing.isPresent()) {
                FoodBox e = existing.get();
                e.setQuantity(e.getQuantity() + item.getQuantity());
            } else {
                foodBoxRepository.save(FoodBox.builder()
                        .userId(userId)
                        .name(item.getName())
                        .quantity(item.getQuantity())
                        .expiryDate(item.getExpiryDate())
                        .build());
            }
        }
    }
}

