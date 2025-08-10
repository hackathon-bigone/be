package hackathon.bigone.sunsak.foodbox.foodbox.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class FoodBoxImminentResponse { //임박 조회를 위한 클래스
    @JsonProperty("food_id")
    private Long foodId;
    private String name;
    private int quantity;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yy.MM.dd")
    private LocalDate expiryDate;

    @JsonIgnore
    private int daysLeft;      //디데이 계산

    private String dLabel; //D-1, D+1 보내기
}
