package hackathon.bigone.sunsak.recipe.board.dto;

import hackathon.bigone.sunsak.accounts.user.entity.SiteUser;
import hackathon.bigone.sunsak.global.util.DisplayDateUtil;
import hackathon.bigone.sunsak.recipe.board.entity.Board;
import hackathon.bigone.sunsak.recipe.board.enums.RecipeCategory;
import hackathon.bigone.sunsak.recipe.comment.dto.CommentResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class BoardResponseDto {
    private Long postId;
    private String title;
    private String cookingTime;
    private String mainImageUrl;
    private List<StepResponseDto> steps;
    private List<RecipeLinkResponseDto> recipeLinks;
    private List<IngredientResponseDto> ingredients;
    private List<RecipeCategory> categories;
    private String recipeDescription;
    private String createdAt;
    private List<CommentResponseDto> comments;
    private int likeCount;
    private int commentCount;
    private String authorName;

    public BoardResponseDto(Board board, List<CommentResponseDto> comments) {
        this.postId = board.getPostId();
        this.title = board.getTitle();
        this.cookingTime = board.getCookingTime();
        this.mainImageUrl = board.getMainImageUrl();
        this.authorName = board.getAuthor() != null
                ? board.getAuthor().getNickname() // 🔹 nickname 사용
                : null;

        this.steps = board.getSteps().stream()
                .map(StepResponseDto::new)
                .collect(Collectors.toList());

        this.recipeLinks = board.getRecipeLink().stream()
                .map(RecipeLinkResponseDto::new)
                .collect(Collectors.toList());

        this.ingredients = board.getIngredients().stream()
                .map(IngredientResponseDto::new)
                .collect(Collectors.toList());

        this.categories = board.getCategories();
        this.recipeDescription = board.getRecipeDescription();
        this.createdAt = DisplayDateUtil.toDisplay(board.getCreateDate());
        this.comments = comments;

        this.likeCount = board.getLikes().size();
        this.commentCount = board.getComments().size();
    }

    public BoardResponseDto(Board board) {
        this.postId = board.getPostId();
        this.title = board.getTitle();
        this.cookingTime = board.getCookingTime();
        this.mainImageUrl = board.getMainImageUrl();
        this.authorName = board.getAuthor() != null
                ? board.getAuthor().getNickname() // 🔹 nickname 사용
                : null;

        this.steps = board.getSteps().stream()
                .map(StepResponseDto::new)
                .collect(Collectors.toList());

        this.recipeLinks = board.getRecipeLink().stream()
                .map(RecipeLinkResponseDto::new)
                .collect(Collectors.toList());

        this.ingredients = board.getIngredients().stream()
                .map(IngredientResponseDto::new)
                .collect(Collectors.toList());

        this.categories = board.getCategories();
        this.recipeDescription = board.getRecipeDescription();
        this.createdAt = DisplayDateUtil.toDisplay(board.getCreateDate());


        this.likeCount = board.getLikes().size();
        this.commentCount = board.getComments().size();
    }
}