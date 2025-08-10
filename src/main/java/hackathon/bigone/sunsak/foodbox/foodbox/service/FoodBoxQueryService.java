package hackathon.bigone.sunsak.foodbox.foodbox.service;

//조회하는 역할

import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodBoxResponse;
import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodListResponse;
import hackathon.bigone.sunsak.foodbox.foodbox.entity.FoodBox;
import hackathon.bigone.sunsak.foodbox.foodbox.repository.FoodBoxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodBoxQueryService { //조회하는 기능

    private final FoodBoxRepository foodBoxRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter KOREAN_DATE = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    private static final int DEFAULT_IMMINENT_DAYS = 7;

    // 로그인한 유저의 foodbox 조회
    @Transactional(readOnly = true)
    public FoodListResponse getFoodsByUser(Long userId, String filter, Integer days) {
        if (userId == null) throw new IllegalArgumentException("userId가 없습니다.");

        boolean isImminent = "imminent".equalsIgnoreCase(filter);

        final int threshold = (days == null) ? DEFAULT_IMMINENT_DAYS : days;
        if (isImminent && threshold < 0) {
            throw new IllegalArgumentException("days 는 0 이상이어야 합니다.");
        }

        LocalDate today = LocalDate.now(KST);
        String todayStr = today.format(KOREAN_DATE);

        List<FoodBox> rows = foodBoxRepository.findAllSortedByUserId(userId);

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
        return getFoodsByUser(userId, "all", null).getItems(); //모든 식품 조회
    }

}
