package hackathon.bigone.sunsak.home.service;

import hackathon.bigone.sunsak.foodbox.foodbox.dto.FoodBoxImminentResponse;
import hackathon.bigone.sunsak.foodbox.foodbox.service.FoodBoxQueryService;
import hackathon.bigone.sunsak.home.dto.HomeFoodDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final FoodBoxQueryService foodBoxQueryService;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter KOREAN_DATE = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

    private String todayKor() {
        return LocalDate.now(KST).format(KOREAN_DATE);
    }

    public HomeFoodDto getImminentFoods(Long userId) {
        var imminentList = foodBoxQueryService.getImminentList(userId, 7).getItems()
                .stream()
                .filter(item -> !item.getDLabel().startsWith("D+")) // D+ 제거
                .toList();

        if (imminentList.isEmpty()) {
            return HomeFoodDto.builder()
                    .today(todayKor())
                    .message("유통기한 임박 식품이 아직 없어요!")
                    .summary(null)
                    .dLabel("안전")
                    .build();
        }

        if (imminentList.size() == 1) { //음식이 1개일 때
            var only = imminentList.get(0);
            return HomeFoodDto.builder()
                    .today(todayKor())
                    .message("유통기한이 얼마 남지 않았어요!")
                    .summary(only.getName() + " " + only.getQuantity() + "개")
                    .dLabel(only.getDLabel())
                    .build();
        }

        int minDaysLeft = imminentList.get(0).getDaysLeft(); // 가장 빠른 값 찾기
        List<FoodBoxImminentResponse> sameDayFoods = imminentList.stream()
                .filter(f -> f.getDaysLeft() == minDaysLeft)
                .toList();

        var first = sameDayFoods.get(0);

        String summary = (sameDayFoods.size() == 1)
                ? first.getName() + " " + first.getQuantity() + "개"
                : first.getName() + " 외 " + (sameDayFoods.size() - 1) + "개";


        return HomeFoodDto.builder()
                .today(todayKor())
                .message("유통기한이 얼마 남지 않았어요!")
                .summary(summary)
                .dLabel(first.getDLabel()) // D-0, D-1 등
                .build();
    }

    public HomeFoodDto getGuestFoodBox() {
        return HomeFoodDto.builder()
                .today(todayKor())
                .message("로그인하고 \n" +
                        "순삭의 다양한 서비스를\n" +
                        "경험해보세요!")
                .summary(null)
                .build();
    }
}
