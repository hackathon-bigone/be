package hackathon.bigone.sunsak.foodbox.foodbox.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FoodListResponse<T> {
    private String today;  //식품 조회 헤더 2025년 08월 09일로 보이게
    private List<T> items;
}
