package hackathon.bigone.sunsak.recipe.board.dto;

import hackathon.bigone.sunsak.recipe.board.enums.RecipeCategory;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
@Getter
@Setter
public class BoardDto {
    private String title;
    private String serving;
    private String cookingTime;
    private String imageUrl;
    private List<StepDto> steps;
    private List<RecipeLinkDto> recipeLinks;
    private List<IngredientDto> ingredients;
    private RecipeCategory category;
    private String description;

}
