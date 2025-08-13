package hackathon.bigone.sunsak.recipe.board.dto;

import hackathon.bigone.sunsak.recipe.board.entity.RecipeLink;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeLinkResponseDto {
    private Long recipelinkId;
    private String recipelinkUrl;

    public RecipeLinkResponseDto(RecipeLink recipeLink) {
        this.recipelinkId = recipeLink.getRecipelinkId();
        this.recipelinkUrl = recipeLink.getRecipelinkUrl();
    }
}