package hackathon.bigone.sunsak.recipe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngredientRequestDto {
    private String ingredientName;
    private String ingredientAmount;
}