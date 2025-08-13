package hackathon.bigone.sunsak.recipe.board.dto;

import hackathon.bigone.sunsak.recipe.board.entity.Ingredient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngredientResponseDto {
    private Long ingredientId;
    private String ingredientName;
    private String ingredientAmount;

    public IngredientResponseDto(Ingredient ingredient) {
        this.ingredientId = ingredient.getIngredientId();
        this.ingredientName = ingredient.getIngredientName();
        this.ingredientAmount = ingredient.getIngredientAmount();
    }
}