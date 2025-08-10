package hackathon.bigone.sunsak.global.initData;

import hackathon.bigone.sunsak.foodbox.foodbox.entity.FoodBox;
import hackathon.bigone.sunsak.foodbox.foodbox.repository.FoodBoxRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.LocalDate;
import java.util.List;

@Configuration
@AllArgsConstructor
public class FoodBoxInit {
    private FoodBoxRepository foodBoxRepository;

    @Bean
    @Order(5)
    public ApplicationRunner initFoodBoxes(){
        return args -> {
            if(foodBoxRepository.count() == 0){
                insertDefaultFoodItems();
            }
        };
    }

    private void insertDefaultFoodItems() {
        List<FoodBox> initFoods = List.of(
                new FoodBox(1L, "감자", 3, LocalDate.now().minusDays(1)),
                new FoodBox(1L, "애호박", 2, LocalDate.now().plusDays(0)),// 생성자 순서 엔티티에 맞게
                new FoodBox(1L, "우유(1L)", 1, LocalDate.now().plusDays(1)),
                new FoodBox(1L, "치킨 샐러드", 1, LocalDate.now().plusDays(1)),
                new FoodBox(1L, "슬라이스 치즈", 3, LocalDate.now().plusDays(1)),
                new FoodBox(1L, "참외", 2, LocalDate.now().plusDays(3)),
                new FoodBox(1L, "약과(5개입)", 2, LocalDate.now().plusDays(7)),
                new FoodBox(1L, "키위", 5, LocalDate.now().plusDays(8)),
                new FoodBox(1L, "두부(찌개용)", 2, LocalDate.now().plusDays(8)),
                new FoodBox(1L, "시리얼", 1, LocalDate.now().plusDays(400))
        );

        foodBoxRepository.saveAll(initFoods);
    }
}
