package hackathon.bigone.sunsak.foodbox.foodbox.service;

//조회하는 역할

import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodBoxImminentResponse;
import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodBoxResponse;
import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodListResponse;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FoodBoxQueryService { //조회하는 기능

    private final FoodBoxRepository foodBoxRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter KOREAN_DATE = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    private static final int DEFAULT_IMMINENT_DAYS = 7;

    private String todayKor() { return LocalDate.now(KST).format(KOREAN_DATE); } //헤더 날짜

    private static String toDLabel(int d) {
        if (d >= 0) return "D-" + d; //D-0 으로 수정
        return "D+" + Math.abs(d);
    }

    @Transactional(readOnly = true)
    public FoodListResponse<FoodBoxResponse> getAllList(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId가 없습니다.");

        List<FoodBoxResponse> items = foodBoxRepository.findAllSortedByUserId(userId).stream()
                .map(f -> FoodBoxResponse.builder()
                        .foodId(f.getId())
                        .name(f.getName())
                        .quantity(f.getQuantity() == null ? 0 : f.getQuantity())
                        .expiryDate(f.getExpiryDate())
                        .build()
                )
                .sorted(Comparator
                        .comparing(FoodBoxResponse::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(FoodBoxResponse::getName, Comparator.nullsLast(Comparator.naturalOrder()))
                )
                .toList();

        return FoodListResponse.<FoodBoxResponse>builder()
                .today(todayKor())
                .items(items)
                .message(items.isEmpty()? "식품 목록을 추가해보세요" : null)
                .build();
    }

    @Transactional(readOnly = true)
    public FoodListResponse<FoodBoxImminentResponse> getImminentList(Long userId, Integer days) {
        if (userId == null) throw new IllegalArgumentException("userId가 없습니다.");
        final int threshold = (days == null ? DEFAULT_IMMINENT_DAYS : Math.max(0, days));
        final LocalDate today = LocalDate.now(KST);

        List<FoodBoxImminentResponse> items = foodBoxRepository.findAllSortedByUserId(userId).stream()
                .filter(f -> f.getExpiryDate() != null)
                .map(f -> {
                    int d = (int) ChronoUnit.DAYS.between(today, f.getExpiryDate()); // 음수 허용
                    if (Math.abs(d) > threshold) return null; // threshold 밖이면 제외
                    return FoodBoxImminentResponse.builder()
                            .foodId(f.getId())
                            .name(f.getName())
                            .quantity(f.getQuantity() == null ? 0 : f.getQuantity())
                            .expiryDate(f.getExpiryDate())
                            .daysLeft(d)                        // 원시값
                            .dLabel(toDLabel(d))                // 디데이로 포맷 바꿈
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparingInt(FoodBoxImminentResponse::getDaysLeft)
                        .thenComparing(FoodBoxImminentResponse::getName, Comparator.nullsLast(Comparator.naturalOrder()))
                )
                .toList();

        return FoodListResponse.<FoodBoxImminentResponse>builder()
                .today(todayKor())
                .items(items)
                .message(items.isEmpty()? "유통기한 임박 목록이 없어요" : null)
                .build();
    }

    @Transactional(readOnly = true)
    public List<FoodBoxResponse> getFoodsByUserList(Long userId) {
        return getAllList(userId).getItems();
    }

}
