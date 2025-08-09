package hackathon.bigone.sunsak.recipe.board.enums;

import lombok.Getter;

@Getter
public enum RecipeCategory {
    BEGINNER("왕초보"),
    MICROWAVE_AIRFRYER("전자레인지/에어프라이어"),
    DESSERT("디저트"),
    VEGAN("비건");

    private final String displayName;

    RecipeCategory(String displayName) {
        this.displayName = displayName;
    }

}