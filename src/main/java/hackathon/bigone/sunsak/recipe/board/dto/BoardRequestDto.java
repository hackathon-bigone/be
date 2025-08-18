package hackathon.bigone.sunsak.recipe.board.dto;

import hackathon.bigone.sunsak.recipe.board.enums.RecipeCategory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class BoardRequestDto {
    private String title;
    private int cookingTime;
    private MultipartFile mainImageFile;
    private String mainImageUrl;
    private List<StepRequestDto> steps;
    private List<RecipeLinkRequestDto> recipeLinks;
    private List<IngredientRequestDto> ingredients;
    private List<RecipeCategory> categories;
    private String recipeDescription;
}