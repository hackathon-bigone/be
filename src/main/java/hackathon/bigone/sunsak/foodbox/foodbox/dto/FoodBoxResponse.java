package hackathon.bigone.sunsak.foodbox.foodbox.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@JsonPropertyOrder({ "food_id", "name", "quantity", "expiryDate" }) //응답 조정
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodBoxResponse {
    @JsonProperty("food_id")
    private Long foodId;

    @NotBlank
    private String name;
    private int quantity;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yy.MM.dd")
    private LocalDate expiryDate;

}
