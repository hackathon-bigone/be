package hackathon.bigone.sunsak.recipe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class IngredientDto {
    private Long id;
    private String name;
    private String amount;
}
