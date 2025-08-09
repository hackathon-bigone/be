package hackathon.bigone.sunsak.foodbox.foodbox.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import hackathon.bigone.sunsak.foodbox.foodbox.entity.FoodBox;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class FoodBoxResponse {
    @JsonProperty("food_id")
    private Long foodId;

    @NotBlank
    private String name;
    private int quantity;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yy.MM.dd")
    private LocalDate expiryDate;

    private Integer daysLeft;                     // D-값(오늘=0), null 가능
    private boolean imminent;                  // daysLeft 기준

    public static FoodBoxResponse of(FoodBox f, Integer daysLeft){
        return FoodBoxResponse.builder()
                .foodId(f.getId())
                .name(f.getName())
                .quantity(f.getQuantity())
                .expiryDate(f.getExpiryDate())
                .daysLeft(daysLeft)
                .imminent(daysLeft != null && daysLeft >= 0 && daysLeft < 7)
                .build();
    }
}
